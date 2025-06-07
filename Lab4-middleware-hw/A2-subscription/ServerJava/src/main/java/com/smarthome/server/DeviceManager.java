package com.smarthome.server;

import com.smarthome.services.SmartHome.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceManager implements IDeviceManager {
    private final List<DeviceBase> devices = new ArrayList<>();
    private String serverName;

    public DeviceManager(String serverName) {
        this.serverName = serverName;
        initializeDevices();
    }

    private void initializeDevices() {
        if (serverName.equals("Indoor Server")) {
            Thermostat thermostat1 = new Thermostat();
            thermostat1.setId("thermo-1");
            thermostat1.setName("Kids Room Thermostat");
            thermostat1.setType(DeviceType.THERMOSTAT);
            thermostat1.setSubType("Indoor");
            thermostat1.setOnline(true);
            thermostat1.setTemperatureUnit("Celsius");
            thermostat1.setCurrentTemperature(20.0f);
            thermostat1.setTargetTemperature(22.0f);
            thermostat1.setLocation("Kids Room");
            thermostat1.setBatteryLevel(68);
            devices.add(thermostat1);

            Refrigerator fridge = new Refrigerator();
            fridge.setId("fridge-1");
            fridge.setName("Kitchen Refrigerator");
            fridge.setType(DeviceType.REFRIGERATOR);
            fridge.setSubType("Smart");
            fridge.setOnline(true);
            fridge.setMode(RefrigeratorInfo.Mode.NORMAL);
            fridge.setCurrentTemperature(4.0f);
            fridge.setDoorOpen(false);

            List<RefrigeratorCompartment> compartments = new ArrayList<>();

            RefrigeratorCompartment mainComp = new RefrigeratorCompartment();
            mainComp.setName("Main Compartment");
            mainComp.setCurrentTemperature(4.0f);
            mainComp.setTargetTemperature(3.0f);
            compartments.add(mainComp);

            RefrigeratorCompartment freezerComp = new RefrigeratorCompartment();
            freezerComp.setName("Freezer Compartment");
            freezerComp.setCurrentTemperature(-18.0f);
            freezerComp.setTargetTemperature(-20.0f);
            compartments.add(freezerComp);

            fridge.setCompartments(compartments);
            devices.add(fridge);

            Thermostat thermostat2 = new Thermostat();
            thermostat2.setId("thermo-2");
            thermostat2.setName("Living Room Thermostat");
            thermostat2.setType(DeviceType.THERMOSTAT);
            thermostat2.setSubType("Indoor");
            thermostat2.setOnline(true);
            thermostat2.setTemperatureUnit("Celsius");
            thermostat2.setCurrentTemperature(21.0f);
            thermostat2.setTargetTemperature(23.0f);
            thermostat2.setLocation("Living Room");
            thermostat2.setBatteryLevel(75);
            devices.add(thermostat2);
        }

        if (serverName.equals("Outdoor Server")) {
            SecurityCamera camera1 = new SecurityCamera();
            camera1.setId("camera-1");
            camera1.setName("Front Yard Camera");
            camera1.setType(DeviceType.SECURITY_CAMERA);
            camera1.setSubType("Outdoor");
            camera1.setOnline(true);
            camera1.setLocation("Front Yard");
            camera1.setRecording(true);

            Position pos1 = Position.newBuilder()
                    .setPan(180)
                    .setTilt(45)
                    .setZoom(5)
                    .build();
            camera1.setPosition(pos1);
            camera1.setBatteryLevel(85);
            devices.add(camera1);

            SecurityCamera camera2 = new SecurityCamera();
            camera2.setId("camera-2");
            camera2.setName("Back Yard Camera");
            camera2.setType(DeviceType.SECURITY_CAMERA);
            camera2.setSubType("Outdoor");
            camera2.setOnline(true);
            camera2.setLocation("Back Yard");
            camera2.setRecording(false);

            com.smarthome.services.SmartHome.Position pos2 = com.smarthome.services.SmartHome.Position.newBuilder()
                    .setPan(90)
                    .setTilt(30)
                    .setZoom(3)
                    .build();
            camera2.setPosition(pos2);
            camera2.setBatteryLevel(90);
            devices.add(camera2);
        }
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public List<DeviceBase> getDevices() {
        return devices;
    }

    @Override
    public DeviceBase getDeviceById(String deviceId) {
        for (DeviceBase device : devices) {
            if (device.getId().equals(deviceId)) {
                return device;
            }
        }
        return null;
    }

    @Override
    public DeviceBase getDeviceByName(String deviceName) {
        for (DeviceBase device : devices) {
            if (device.getName().equals(deviceName)) {
                return device;
            }
        }
        return null;
    }

    @Override
    public List<DeviceBase> getDevicesByType(DeviceType deviceType) {
        return devices.stream()
                .filter(device -> device.getType() == deviceType)
                .collect(Collectors.toList());
    }

    @Override
    public boolean tryControlDevice(ControlRequest request, MessageHolder messageHolder) {
        DeviceBase device = getDeviceById(request.getDeviceId());
        if (device == null) {
            messageHolder.setMessage(String.format("Device with ID %s not found", request.getDeviceId()));
            return false;
        }

        return device.tryHandleControlRequest(request, messageHolder);
    }
}
