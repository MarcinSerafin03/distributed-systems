package com.smarthome.server;

import com.smarthome.services.SmartHome.*;
import java.util.List;

public interface IDeviceManager {
    String getServerName();
    List<DeviceBase> getDevices();
    DeviceBase getDeviceById(String deviceId);
    DeviceBase getDeviceByName(String deviceName);
    List<DeviceBase> getDevicesByType(DeviceType deviceType);
    boolean tryControlDevice(ControlRequest request, MessageHolder messageHolder);
}