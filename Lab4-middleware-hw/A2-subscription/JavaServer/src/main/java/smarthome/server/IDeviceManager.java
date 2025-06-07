package smarthome.server;

import java.util.List;
import smarthome.services.DeviceType;
import smarthome.services.ControlRequest;

public interface IDeviceManager {
    String getServerName();
    List<DeviceBase> getDevices();
    DeviceBase getDeviceById(String deviceId);
    DeviceBase getDeviceByName(String deviceName);
    List<DeviceBase> getDevicesByType(DeviceType deviceType);
    boolean tryControlDevice(ControlRequest request, OutParam<String> message);
}
