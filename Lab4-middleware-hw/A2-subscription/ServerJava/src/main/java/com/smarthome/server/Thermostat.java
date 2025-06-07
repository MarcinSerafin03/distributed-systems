package com.smarthome.server;

import com.smarthome.services.SmartHome.ThermostatInfo;
import com.smarthome.services.SmartHome.DeviceStatus;
import com.smarthome.services.SmartHome.Device;
import com.smarthome.services.SmartHome.DeviceInfoResponse;
import com.smarthome.services.SmartHome.ControlRequest;

import java.util.Random;

public class Thermostat extends DeviceBase {
    private String temperatureUnit;
    private float currentTemperature;
    private float targetTemperature;
    private String location;
    private int batteryLevel;

    private final Random random = new Random();

    public String getTemperatureUnit() {
        return temperatureUnit;
    }

    public void setTemperatureUnit(String temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }

    public float getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(float currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    public float getTargetTemperature() {
        return targetTemperature;
    }

    public void setTargetTemperature(float targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    @Override
    public DeviceInfoResponse getDeviceInfo() {
        Device device = Device.newBuilder()
                .setId(id)
                .setName(name)
                .setType(type)
                .setSubType(subType)
                .setOnline(isOnline)
                .build();

        ThermostatInfo thermostatInfo = ThermostatInfo.newBuilder()
                .setTemperatureUnit(temperatureUnit)
                .setCurrentTemperature(currentTemperature)
                .setTargetTemperature(targetTemperature)
                .setLocation(location)
                .setBatteryLevel(batteryLevel)
                .build();

        return DeviceInfoResponse.newBuilder()
                .setDevice(device)
                .setThermostatInfo(thermostatInfo)
                .build();
    }

    @Override
    public DeviceStatus getDeviceStatus(String timestamp) {
        // Simulate current temperature
        currentTemperature = (float)(random.nextDouble() * 30);
        // Simulate target temperature
        targetTemperature = (float)(random.nextDouble() * 30);

        ThermostatInfo thermostatInfo = ThermostatInfo.newBuilder()
                .setTemperatureUnit(temperatureUnit)
                .setCurrentTemperature(currentTemperature)
                .setTargetTemperature(targetTemperature)
                .setLocation(location)
                .setBatteryLevel(batteryLevel)
                .build();

        return DeviceStatus.newBuilder()
                .setDeviceId(id)
                .setDeviceType(type)
                .setIsOnline(isOnline)
                .setThermostatInfo(thermostatInfo)
                .build();
    }

    @Override
    public boolean tryHandleControlRequest(ControlRequest request, MessageHolder messageHolder) {
        if (request.hasSetTemperature()) {
            targetTemperature = request.getSetTemperature().getTemperature();
            messageHolder.setMessage(String.format("Target temperature set to %s°%s",
                    targetTemperature, temperatureUnit));
            return true;
        } else {
            messageHolder.setMessage("No target temperature provided.");
            return false;
        }
    }
}