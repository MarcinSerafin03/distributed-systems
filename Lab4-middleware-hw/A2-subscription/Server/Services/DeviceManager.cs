namespace SmartHome.Server
{
    public class DeviceManager : IDeviceManager
    {
        private readonly List<DeviceBase> _devices = new List<DeviceBase>();
        public string ServerName { get; private set; }

        public DeviceManager(string serverName)
        {
            ServerName = serverName;
            InitalizeDevices();
        }
        
        private void InitalizeDevices()
        {
            if (ServerName == "Indoor Server")
            {
                _devices.Add(new Thermostat
                {
                    id = "thermo-1",
                    Name = "Kids Room Thermostat",
                    Type = DeviceType.Thermostat,
                    subType = "Indoor",
                    IsOnline = true,
                    TemperatureUnit = "Celsius",
                    CurrentTemperature = 20.0f,
                    TargetTemperature = 22.0f,
                    Location = "Kids Room",
                    BatteryLevel = 68
                });
                
                _devices.Add(new Refrigerator
                {
                    id = "fridge-1",
                    Name = "Kitchen Refrigerator",
                    Type = DeviceType.Refrigerator,
                    subType = "Smart",
                    IsOnline = true,
                    Mode = RefrigeratorInfo.Types.Mode.Normal,
                    CurrentTemperature = 4.0f,
                    DoorOpen = false,
                    Compartments = new List<RefrigeratorCompartment>
                    {
                        new RefrigeratorCompartment
                        {
                            Name = "Main Compartment",
                            CurrentTemperature = 4.0f,
                            TargetTemperature = 3.0f
                        },
                        new RefrigeratorCompartment
                        {
                            Name = "Freezer Compartment",
                            CurrentTemperature = -18.0f,
                            TargetTemperature = -20.0f
                        }
                    }
                });
                
                _devices.Add(new Thermostat
                {
                    id = "thermo-2",
                    Name = "Living Room Thermostat",
                    Type = DeviceType.Thermostat,
                    subType = "Indoor",
                    IsOnline = true,
                    TemperatureUnit = "Celsius",
                    CurrentTemperature = 21.0f,
                    TargetTemperature = 23.0f,
                    Location = "Living Room",
                    BatteryLevel = 75
                });
            }

            if (ServerName == "Outdoor Server")
            {
                _devices.Add(new SecurityCamera
                {
                    id = "camera-1",
                    Name = "Front Yard Camera",
                    Type = DeviceType.SecurityCamera,
                    subType = "Outdoor",
                    IsOnline = true,
                    Location = "Front Yard",
                    Recording = true,
                    Position = new PtzPosition
                    {
                        Pan = 180,
                        Tilt = 45,
                        Zoom = 5
                    },
                    BatteryLevel = 85,
                });
                _devices.Add(new SecurityCamera
                {
                    id = "camera-2",
                    Name = "Back Yard Camera",
                    Type = DeviceType.SecurityCamera,
                    subType = "Outdoor",
                    IsOnline = true,
                    Location = "Back Yard",
                    Recording = false,
                    Position = new PtzPosition
                    {
                        Pan = 90,
                        Tilt = 30,
                        Zoom = 3
                    },
                    BatteryLevel = 90,
                });
            }
        }
        
        public List<DeviceBase> Devices => _devices;
        public DeviceBase GetDeviceById(string deviceId)
        {
            return _devices.FirstOrDefault(device => device.id == deviceId);
        }
        
        public DeviceBase GetDeviceByName(string deviceName)
        {
            return _devices.FirstOrDefault(device => device.Name == deviceName);
        }
        
        public List<DeviceBase> GetDevicesbyType(DeviceType deviceType)
        {
            return _devices.Where(device => device.Type == deviceType).ToList();
        }

        public bool TryControlDevice(ControlRequest request, out string message)
        {
            var device = GetDeviceById(request.DeviceId);
            if (device == null)
            {
                message = $"Device with ID {request.DeviceId} not found";
                return false;
            }

            return device.TryHandleControlRequest(request, out message);
        }
    }
}