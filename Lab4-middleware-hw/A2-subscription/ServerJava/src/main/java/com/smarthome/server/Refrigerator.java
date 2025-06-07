package com.smarthome.server;

import com.smarthome.services.SmartHome.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Refrigerator extends DeviceBase {
    private RefrigeratorInfo.Mode mode = RefrigeratorInfo.Mode.NORMAL;
    private float currentTemperature;
    private boolean doorOpen;
    private List<RefrigeratorCompartment> compartments = new ArrayList<>();

    private final Random random = new Random();

    public RefrigeratorInfo.Mode getMode() {
        return mode;
    }

    public void setMode(RefrigeratorInfo.Mode mode) {
        this.mode = mode;
    }

    public float getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(float currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    public boolean isDoorOpen() {
        return doorOpen;
    }

    public void setDoorOpen(boolean doorOpen) {
        this.doorOpen = doorOpen;
    }

    public List<RefrigeratorCompartment> getCompartments() {
        return compartments;
    }

    public void setCompartments(List<RefrigeratorCompartment> compartments) {
        this.compartments = compartments;
    }

    @Override
    public DeviceInfoResponse getDeviceInfo() {
        simulateTemperatureFluctuations();

        Device device = Device.newBuilder()
                .setId(id)
                .setName(name)
                .setType(type)
                .setSubType(subType)
                .setOnline(isOnline)
                .build();

        return DeviceInfoResponse.newBuilder()
                .setDevice(device)
                .setRefrigeratorInfo(createRefrigeratorInfo())
                .build();
    }

    @Override
    public DeviceStatus getDeviceStatus(String timestamp) {
        simulateTemperatureFluctuations();

        return DeviceStatus.newBuilder()
                .setDeviceId(id)
                .setDeviceType(type)
                .setIsOnline(isOnline)
                .setRefrigeratorInfo(createRefrigeratorInfo())
                .build();
    }

    private RefrigeratorInfo createRefrigeratorInfo() {
        RefrigeratorInfo.Builder infoBuilder = RefrigeratorInfo.newBuilder()
                .setMode(mode)
                .setCurrentTemperature(currentTemperature)
                .setDoorOpen(doorOpen);

        for (RefrigeratorCompartment compartment : compartments) {
            Compartment.Builder compBuilder = Compartment.newBuilder()
                    .setName(compartment.getName())
                    .setCurrentTemperature(compartment.getCurrentTemperature())
                    .setTargetTemperature(compartment.getTargetTemperature());
            infoBuilder.addCompartments(compBuilder.build());
        }

        return infoBuilder.build();
    }

    private void simulateTemperatureFluctuations() {
        // Simulate temperature fluctuations
        currentTemperature += (float)(random.nextDouble() * 2 - 1); // Random change between -1 and +1

        for (RefrigeratorCompartment compartment : compartments) {
            float diff = compartment.getTargetTemperature() - compartment.getCurrentTemperature();
            float adjustment = (float)(diff * 0.1 + (random.nextDouble() * 0.2 - 0.1));
            compartment.setCurrentTemperature(compartment.getCurrentTemperature() + adjustment);
        }
    }

    @Override
    public boolean tryHandleControlRequest(ControlRequest request, MessageHolder messageHolder) {
        if (request.getControlCommandCase() == ControlRequest.ControlCommandCase.SET_TEMPERATURE) {
            SetTemperature action = request.getSetTemperature();

            // Find the compartment by name
            RefrigeratorCompartment targetCompartment = null;
            for (RefrigeratorCompartment comp : compartments) {
                if (comp.getName().equals(action.getCompartmentName())) {
                    targetCompartment = comp;
                    break;
                }
            }

            if (targetCompartment == null) {
                messageHolder.setMessage(String.format("Compartment '%s' not found", action.getCompartmentName()));
                return false;
            }

            targetCompartment.setTargetTemperature(action.getTemperature());
            messageHolder.setMessage(String.format("Set target temperature of %s to %s°C",
                    action.getCompartmentName(), action.getTemperature()));
            return true;
        }
        else if (request.getControlCommandCase() == ControlRequest.ControlCommandCase.SET_MODE) {
            SetMode action = request.getSetMode();
            mode = action.getMode();
            messageHolder.setMessage(String.format("Set refrigerator mode to %s", action.getMode()));
            return true;
        }
        else {
            messageHolder.setMessage("Unsupported control action for refrigerator");
            return false;
        }
    }
}