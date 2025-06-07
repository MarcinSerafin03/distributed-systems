import sys
import time
import logging
import grpc
import threading
from concurrent.futures import ThreadPoolExecutor

# Import generated gRPC stubs
import smart_home_pb2 as pb
import smart_home_pb2_grpc as pb_grpc

logging.basicConfig(
    level=logging.INFO, 
    format='%(asctime)s - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)

class SmartHomeClient:
    def __init__(self, server_address='localhost:50051'):
        self.server_address = server_address
        self.channel = None
        self.stub = None
        self.monitor_threads = {}
        self.reconnect_event = threading.Event()
        self.connected = False

        
        threading.Thread(target=self._reconnection_worker, daemon=True).start()

        self._connect()

    def _connect(self):
        try:
            logger.info(f"Connecting to server at {self.server_address}...")
            self.channel = grpc.insecure_channel(
                self.server_address,
                options=[
                    ('grpc.keepalive_time_ms', 10000),             # Send keepalive ping every 10 seconds
                    ('grpc.keepalive_timeout_ms', 5000),           # Keepalive ping timeout after 5 seconds
                    ('grpc.keepalive_permit_without_calls', True), # Allow keepalive pings when there's no active calls
                    ('grpc.http2.max_pings_without_data', 0),      # Allow unlimited pings without data
                    ('grpc.http2.min_time_between_pings_ms', 10000), # Minimum time between pings
                    ('grpc.http2.min_ping_interval_without_data_ms', 5000) # Minimum time between pings without data
                ]
                )
            self.stub = pb_grpc.SmartHomeServiceStub(self.channel)
            self.list_devices()
            
            logger.info("Connected to server.")
            self.connected = True
            self.reconnect_event.set()

        except grpc.RpcError as e:
            logger.error(f"Failed to connect to server: {e.code()} - {e.details()}")
            self.connected = False
            self.reconnect_event.clear()

    def _reconnection_worker(self):
        while True:
            # If not connected, try to reconnect every 5 seconds
            if not self.connected:
                logger.info("Attempting to reconnect...")
                self._connect()
                
            time.sleep(5)  # Wait 5 seconds before checking connection again

    def list_devices(self, device_type=None):
        """List all available devices or filter by type"""
        try:
            request = pb.ListDevicesRequest()
            if device_type:
                request.device_type = device_type
                
            response = self.stub.ListDevices(request)
            return response.devices
            
        except grpc.RpcError as e:
            logger.error(f"Error listing devices: {e}")
            self.connected = False
            self.reconnect_event.clear()
            return []

    def get_device_info(self, device_id):
        """Get detailed information about a specific device"""
        try:
            request = pb.DeviceInfoRequest(device_id=device_id)
            response = self.stub.GetDeviceInfo(request)
            return response
            
        except grpc.RpcError as e:
            logger.error(f"Error getting device info: {e}")
            self.connected = False
            self.reconnect_event.clear()
            return None

    def control_device(self, device_id, control_action):
        """Control a device with a specific action"""
        try:
            request = pb.ControlRequest(device_id=device_id)
            
            # Set the appropriate control action field
            action_type = control_action.get('type')
            if action_type == 'set_temperature':
                request.set_temperature.compartment_name = control_action.get('compartment_name', '')
                request.set_temperature.temperature = control_action.get('temperature', 0.0)
            elif action_type == 'set_refrigerator_mode':
                request.set_refrigerator_mode.mode = control_action.get('mode')
            elif action_type == 'set_ptz_position':
                pos = control_action.get('position', {})
                request.set_ptz_position.position.pan = pos.get('pan', 0.0)
                request.set_ptz_position.position.tilt = pos.get('tilt', 0.0)
                request.set_ptz_position.position.zoom = pos.get('zoom', 1.0)
            elif action_type == 'set_recording_state':
                request.set_recording_state.recording = control_action.get('recording', False)
            else:
                logger.error(f"Unknown control action type: {action_type}")
                return False, f"Unknown control action type: {action_type}"
                
            response = self.stub.ControlDevice(request)
            return response.success, response.message
            
        except grpc.RpcError as e:
            logger.error(f"Error controlling device: {e}")
            self.connected = False
            self.reconnect_event.clear()
            return False, str(e)

    def start_monitoring(self, device_id, update_interval=5, callback=None):
        """Start monitoring a device with streaming updates"""
        if device_id in self.monitoring_threads:
            logger.warning(f"Already monitoring device {device_id}")
            return False
            
        thread = threading.Thread(
            target=self._monitor_device_worker,
            args=(device_id, update_interval, callback),
            daemon=True
        )
        
        self.monitoring_threads[device_id] = thread
        thread.start()
        return True

    def stop_monitoring(self, device_id):
        """Stop monitoring a specific device"""
        if device_id not in self.monitoring_threads:
            logger.warning(f"Not monitoring device {device_id}")
            return False
            
        # Thread will terminate on next iteration
        del self.monitoring_threads[device_id]
        return True

    def _monitor_device_worker(self, device_id, update_interval, callback):
        """Worker thread for monitoring a device"""
        while device_id in self.monitoring_threads:
            try:
                # Wait until connected before proceeding
                if not self.connected:
                    logger.info(f"Waiting for reconnection to monitor device {device_id}")
                    self.reconnect_event.wait()
                    
                request = pb.MonitorRequest(
                    device_id=device_id,
                    update_interval_seconds=update_interval
                )
                
                # Start the streaming call
                stream_response = self.stub.MonitorDevice(request)
                
                # Process streaming updates
                for status in stream_response:
                    if device_id not in self.monitoring_threads:
                        break  # Stop if monitoring was cancelled
                        
                    logger.debug(f"Received status update for device {device_id}")
                    
                    if callback:
                        callback(status)
                    else:
                        self._print_device_status(status)
                        
            except grpc.RpcError as e:
                logger.error(f"Error in monitoring device {device_id}: {e}")
                self.connected = False
                self.reconnect_event.clear()
                
                # Wait before retry
                time.sleep(5)

    def _print_device_status(self, status):
        """Print device status in a human-readable format"""
        print(f"\n=== Device Status: {status.device_id} ===")
        print(f"Type: {pb.DeviceType.Name(status.device_type)}")
        print(f"Online: {status.is_online}")
        print(f"Timestamp: {status.timestamp}")
        
        if status.status_details.WhichOneof('status_details') == 'temperature_sensor':
            sensor = status.temperature_sensor
            print(f"Temperature: {sensor.current_temperature:.1f}°C")
            print(f"Humidity: {sensor.humidity:.1f}%")
            print(f"Location: {sensor.location}")
            print(f"Battery: {sensor.battery_level}%")
            
        elif status.status_details.WhichOneof('status_details') == 'refrigerator':
            fridge = status.refrigerator
            print(f"Temperature: {fridge.current_temperature:.1f}°C")
            print(f"Mode: {pb.RefrigeratorInfo.Mode.Name(fridge.current_mode)}")
            print(f"Door Open: {'Yes' if fridge.door_open else 'No'}")
            
            for comp in fridge.compartments:
                print(f"  - {comp.name}: Current {comp.current_temperature:.1f}°C, Target {comp.target_temperature:.1f}°C")
                
        elif status.status_details.WhichOneof('status_details') == 'camera':
            camera = status.camera
            print(f"Location: {camera.location}")
            print(f"Recording: {'Yes' if camera.recording else 'No'}")
            print(f"Battery: {camera.battery_level}%")
            print(f"Position: Pan {camera.ptz_position.pan:.1f}°, Tilt {camera.ptz_position.tilt:.1f}°, Zoom {camera.ptz_position.zoom:.1f}x")
            
        print("=" * 40)

    def close(self):
        """Close the client connection and clean up resources"""
        # Stop all monitoring threads
        self.monitoring_threads.clear()
        
        if self.channel:
            self.channel.close()
            
        logger.info("Client connection closed")


# Interactive CLI for Smart Home control
def run_cli():
    import argparse
    
    parser = argparse.ArgumentParser(description='Smart Home Control Client')
    parser.add_argument('--server', default='localhost:50051', help='Server address (default: localhost:50051)')
    args = parser.parse_args()
    
    client = SmartHomeClient(args.server)
    
    def print_help():
        print("\nAvailable commands:")
        print("  list [type]                  - List all devices or filter by type")
        print("  info <device_id>             - Get detailed info about a device")
        print("  monitor <device_id> [secs]   - Start monitoring a device (updates every X seconds)")
        print("  stop <device_id>             - Stop monitoring a device")
        print("  control <device_id> ...      - Control a device (with additional parameters)")
        print("    Examples:")
        print("      control fridge-1 temp Main 4.0      - Set Main compartment temperature to 4.0°C")
        print("      control fridge-1 mode eco           - Set refrigerator to ECO mode")
        print("      control camera-1 ptz 180 45 2.0     - Set camera position (pan, tilt, zoom)")
        print("      control camera-1 record on/off      - Start/stop recording")
        print("  servers                      - Try to connect to multiple server instances")
        print("  quit                         - Exit the program")
        print("  help                         - Show this help message")
    
    print("\nSmart Home Control Client")
    print("=========================")
    print(f"Connected to server: {args.server}")
    print_help()
    
    while True:
        try:
            command = input("\n> ").strip().split()
            
            if not command:
                continue
                
            cmd = command[0].lower()
            
            if cmd in ('quit', 'exit', 'q'):
                break
                
            elif cmd == 'help':
                print_help()
                
            elif cmd == 'list':
                device_type = command[1].upper() if len(command) > 1 else None
                devices = client.list_devices(device_type)
                
                if devices:
                    print("\nAvailable devices:")
                    for d in devices:
                        print(f"  {d.id}: {d.name} ({pb.DeviceType.Name(d.type)}/{d.subtype}) - {'Online' if d.is_online else 'Offline'}")
                else:
                    print("No devices found")
                    
            elif cmd == 'info':
                if len(command) < 2:
                    print("Error: Device ID required")
                    continue
                    
                device_id = command[1]
                info = client.get_device_info(device_id)
                
                if info:
                    print(f"\nDevice: {info.basic_info.name} ({info.basic_info.id})")
                    print(f"Type: {pb.DeviceType.Name(info.basic_info.type)}/{info.basic_info.subtype}")
                    print(f"Status: {'Online' if info.basic_info.is_online else 'Offline'}")
                    
                    if info.WhichOneof('device_specific_info') == 'temperature_sensor':
                        sensor = info.temperature_sensor
                        print(f"Temperature: {sensor.current_temperature:.1f}°C")
                        print(f"Humidity: {sensor.humidity:.1f}%")
                        print(f"Location: {sensor.location}")
                        print(f"Battery: {sensor.battery_level}%")
                        
                    elif info.WhichOneof('device_specific_info') == 'refrigerator':
                        fridge = info.refrigerator
                        print(f"Temperature: {fridge.current_temperature:.1f}°C")
                        print(f"Mode: {pb.RefrigeratorInfo.Mode.Name(fridge.current_mode)}")
                        print(f"Door Open: {'Yes' if fridge.door_open else 'No'}")
                        
                        for comp in fridge.compartments:
                            print(f"  - {comp.name}: Current {comp.current_temperature:.1f}°C, Target {comp.target_temperature:.1f}°C")
                            
                    elif info.WhichOneof('device_specific_info') == 'camera':
                        camera = info.camera
                        print(f"Location: {camera.location}")
                        print(f"Recording: {'Yes' if camera.recording else 'No'}")
                        print(f"Battery: {camera.battery_level}%")
                        print(f"Position: Pan {camera.ptz_position.pan:.1f}°, Tilt {camera.ptz_position.tilt:.1f}°, Zoom {camera.ptz_position.zoom:.1f}x")
                        
            elif cmd == 'monitor':
                if len(command) < 2:
                    print("Error: Device ID required")
                    continue
                    
                device_id = command[1]
                interval = int(command[2]) if len(command) > 2 else 5
                
                success = client.start_monitoring(device_id, interval)
                if success:
                    print(f"Started monitoring {device_id} (updates every {interval} seconds)")
                    
            elif cmd == 'stop':
                if len(command) < 2:
                    print("Error: Device ID required")
                    continue
                    
                device_id = command[1]
                success = client.stop_monitoring(device_id)
                
                if success:
                    print(f"Stopped monitoring {device_id}")
                    
            elif cmd == 'control':
                if len(command) < 3:
                    print("Error: Need device_id and control action")
                    continue
                    
                device_id = command[1]
                action_type = command[2].lower()
                
                # Handle different control actions
                if action_type == 'temp' and len(command) >= 5:
                    compartment = command[3]
                    temp = float(command[4])
                    
                    control_action = {
                        'type': 'set_temperature',
                        'compartment_name': compartment,
                        'temperature': temp
                    }
                    
                elif action_type == 'mode' and len(command) >= 4:
                    mode_str = command[3].upper()
                    mode_map = {
                        'NORMAL': pb.RefrigeratorInfo.Mode.NORMAL,
                        'ECO': pb.RefrigeratorInfo.Mode.ECO,
                        'VACATION': pb.RefrigeratorInfo.Mode.VACATION,
                        'QUICK_FREEZE': pb.RefrigeratorInfo.Mode.QUICK_FREEZE
                    }
                    
                    if mode_str not in mode_map:
                        print(f"Error: Unknown mode '{mode_str}'. Valid modes: {', '.join(mode_map.keys())}")
                        continue
                        
                    control_action = {
                        'type': 'set_refrigerator_mode',
                        'mode': mode_map[mode_str]
                    }
                    
                elif action_type == 'ptz' and len(command) >= 6:
                    try:
                        pan = float(command[3])
                        tilt = float(command[4])
                        zoom = float(command[5])
                        
                        control_action = {
                            'type': 'set_ptz_position',
                            'position': {
                                'pan': pan,
                                'tilt': tilt,
                                'zoom': zoom
                            }
                        }
                    except ValueError:
                        print("Error: PTZ values must be numbers")
                        continue
                        
                elif action_type == 'record' and len(command) >= 4:
                    state = command[3].lower()
                    if state not in ('on', 'off', 'true', 'false', '1', '0'):
                        print("Error: Recording state must be 'on' or 'off'")
                        continue
                        
                    recording = state in ('on', 'true', '1')
                    
                    control_action = {
                        'type': 'set_recording_state',
                        'recording': recording
                    }
                    
                else:
                    print("Error: Invalid control command")
                    continue
                    
                success, message = client.control_device(device_id, control_action)
                print(f"{'Success' if success else 'Failed'}: {message}")
                
            elif cmd == 'servers':
                # Try connecting to different server instances
                print("Trying to connect to Server1...")
                server1 = SmartHomeClient("localhost:50051")
                devices1 = server1.list_devices()
                
                print("\nServer1 devices:")
                for d in devices1:
                    print(f"  {d.id}: {d.name} ({pb.DeviceType.Name(d.type)}/{d.subtype})")
                
                print("\nTrying to connect to Server2...")
                server2 = SmartHomeClient("localhost:50052")
                devices2 = server2.list_devices()
                
                print("\nServer2 devices:")
                for d in devices2:
                    print(f"  {d.id}: {d.name} ({pb.DeviceType.Name(d.type)}/{d.subtype})")
                
                server1.close()
                server2.close()
                
            else:
                print(f"Unknown command: {cmd}")
                print_help()
                
        except Exception as e:
            print(f"Error: {e}")
    
    client.close()
    print("Goodbye!")


if __name__ == "__main__":
    run_cli()