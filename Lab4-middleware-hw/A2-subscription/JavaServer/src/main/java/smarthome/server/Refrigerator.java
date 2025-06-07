package smarthome.server;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import smarthome.services.RefrigeratorInfo;
import smarthome.services.Device;
import smarthome.services.DeviceInfoResponse;
import smarthome.services.DeviceStatus;
import smarthome.services.ControlRequest;
import smarthome.services.Compartment;

public class Refrigerator extends DeviceBase {
    private RefrigeratorInfo.Types.Mode mode;
    private float currentTemperature;
    private boolean doorOpen;
    private List<RefrigeratorCompartment> compartments = new ArrayList<>();
    private final Random random = new Random();

    public RefrigeratorInfo.Types.Mode getMode() {
        return mode;
    }

    public void setMode(RefrigeratorInfo.Types.Mode mode) {
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

        DeviceInfoResponse.Builder responseBuilder = DeviceInfoResponse.newBuilder();
        responseBuilder.setDevice(Device.newBuilder()
                .setId(getId())
                .setName(getName())
                .setType(getType())
                .setSubType(getSubType())
                .setIsOnline(isOnline())
                .build());

        responseBuilder.setRefrigerator(createRefrigeratorInfo());

        return responseBuilder.build();
    }

    @Override
    public DeviceStatus getDeviceStatus(String timestamp) {
        simulateTemperatureFluctuations();

        DeviceStatus.Builder statusBuilder = DeviceStatus.newBuilder();
        statusBuilder.setDeviceId(getId())
                .setDeviceType(getType())
                .setIsOnline(isOnline())
                .setTimestamp(timestamp)
                .setRefrigerator(createRefrigeratorInfo());

        return statusBuilder.build();
    }

    private RefrigeratorInfo createRefrigeratorInfo() {
        RefrigeratorInfo.Builder infoBuilder = RefrigeratorInfo.newBuilder()
                .setMode(mode)
                .setCurrentTemperature(currentTemperature)
                .setDoorOpen(doorOpen);

        for (RefrigeratorCompartment compartment : compartments) {
            infoBuilder.addCompartments(Compartment.newBuilder()
                    .setName(compartment.getName())
                    .setCurrentTemperature(compartment.getCurrentTemperature())
                    .setTargetTemperature(compartment.getTargetTemperature())
                    .build());
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
    public boolean tryHandleControlRequest(ControlRequest request, OutParam<String> message) {
        if (request.hasSetTemperature()) {
            ControlRequest.SetTemperature action = request.getSetTemperature();
            RefrigeratorCompartment compartment = null;

            for (RefrigeratorCompartment comp : compartments) {
                if (comp.getName().equals(action.getCompartmentName())) {
                    compartment = comp;
                    break;
                }
            }

            if (compartment == null) {
                message.setValue(String.format("Compartment '%s' not found", action.getCompartmentName()));
                return false;
            }

            compartment.setTargetTemperature(action.getTemperature());
            message.setValue(String.format("Set target temperature of %s to %s°C",
                    action.getCompartmentName(), action.getTemperature()));
            return true;
        }
        else if (request.hasSetRefrigeratorMode()) {
            ControlRequest.SetRefrigeratorMode action = request.getSetRefrigeratorMode();
            setMode(action.getMode());
            message.setValue(String.format("Set refrigerator mode to %s", action.getMode()));
            return true;
        }
        else {
            message.setValue("Unsupported control action for refrigerator");
            return false;
        }
    }
}