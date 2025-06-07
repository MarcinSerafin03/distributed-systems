package smarthome.server;

import java.util.Random;
import smarthome.services.Device;
import smarthome.services.DeviceInfoResponse;
import smarthome.services.DeviceStatus;
import smarthome.services.ControlRequest;
import smarthome.services.ThermostatInfo;

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
        DeviceInfoResponse.Builder responseBuilder = DeviceInfoResponse.newBuilder();
        responseBuilder.setDevice(Device.newBuilder()
                .setId(getId())
                .setName(getName())
                .setType(getType())
                .setSubType(getSubType())
                .setIsOnline(isOnline())
                .build());

        responseBuilder.setThermostat(ThermostatInfo.newBuilder()
                .setTemperatureUnit(temperatureUnit)
                .setCurrentTemperature(currentTemperature)
                .setTargetTemperature(targetTemperature)
                .setLocation(location)
                .setBatteryLevel(batteryLevel)
                .build());

        return responseBuilder.build();
    }

    @Override
    public DeviceStatus getDeviceStatus(String timestamp) {
        currentTemperature = (float)(random.nextDouble() * 30); // Simulate current temperature
        targetTemperature = (float)(random.nextDouble() * 30); // Simulate target temperature

        DeviceStatus.Builder statusBuilder = DeviceStatus.newBuilder();
        statusBuilder.setDeviceId(getId())
                .setDeviceType(getType())
                .setIsOnline(isOnline())
                .setTimestamp(timestamp)
                .setThermostat(ThermostatInfo.newBuilder()
                        .setTemperatureUnit(temperatureUnit)
                        .setCurrentTemperature(currentTemperature)
                        .setTargetTemperature(targetTemperature)
                        .setLocation(location)
                        .setBatteryLevel(batteryLevel)
                        .build());

        return statusBuilder.build();
    }

    @Override
    public boolean tryHandleControlRequest(ControlRequest request, OutParam<String> message) {
        message.setValue("");
        if (request.hasThermostat()) {
            if (request.getThermostat().hasTargetTemperature()) {
                targetTemperature = request.getThermostat().getTargetTemperature();
                message.setValue(String.format("Target temperature set to %s°%s",
                        targetTemperature, temperatureUnit));
            } else {
                message.setValue("No target temperature provided.");
                return false;
            }
        }
        return true;
    }
}