package smarthome.server;

import java.lang.Math;
import smarthome.services.Device;
import smarthome.services.DeviceInfoResponse;
import smarthome.services.DeviceStatus;
import smarthome.services.ControlRequest;
import smarthome.services.SecurityCameraInfo;
import smarthome.services.PtzPosition;

public class SecurityCamera extends DeviceBase {
    private String location;
    private boolean recording;
    private PtzPosition position;
    private int batteryLevel;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public PtzPosition getPosition() {
        return position;
    }

    public void setPosition(PtzPosition position) {
        this.position = position;
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

        responseBuilder.setSecurityCamera(SecurityCameraInfo.newBuilder()
                .setLocation(location)
                .setRecording(recording)
                .setPosition(position)
                .setBatteryLevel(batteryLevel)
                .build());

        return responseBuilder.build();
    }

    @Override
    public DeviceStatus getDeviceStatus(String timestamp) {
        if (recording && batteryLevel < 100) {
            batteryLevel = Math.max(0, batteryLevel - 1);
        }

        DeviceStatus.Builder statusBuilder = DeviceStatus.newBuilder();
        statusBuilder.setDeviceId(getId())
                .setDeviceType(getType())
                .setIsOnline(isOnline())
                .setTimestamp(timestamp)
                .setSecurityCamera(SecurityCameraInfo.newBuilder()
                        .setLocation(location)
                        .setRecording(recording)
                        .setPosition(position)
                        .setBatteryLevel(batteryLevel)
                        .build());

        return statusBuilder.build();
    }

    @Override
    public boolean tryHandleControlRequest(ControlRequest request, OutParam<String> message) {
        if (request.hasSetPtzPosition()) {
            if (!getSubType().equals("PTZ")) {
                message.setValue("This camera does not support PTZ control");
                return false;
            }

            ControlRequest.SetPtzPosition action = request.getSetPtzPosition();
            PtzPosition.Builder newPosition = PtzPosition.newBuilder();

            int pan = Math.min(Math.max(action.getPosition().getPan(), 0), 360);
            int tilt = Math.min(Math.max(action.getPosition().getTilt(), 0), 90);
            int zoom = Math.min(Math.max(action.getPosition().getZoom(), 1), 10);

            newPosition.setPan(pan)
                    .setTilt(tilt)
                    .setZoom(zoom);

            this.position = newPosition.build();

            message.setValue(String.format("Set camera position to Pan=%d, Tilt=%d, Zoom=%d",
                    position.getPan(), position.getTilt(), position.getZoom()));
            return true;
        }
        else if (request.hasSetRecordingState()) {
            ControlRequest.SetRecordingState action = request.getSetRecordingState();
            recording = action.getRecording();
            message.setValue(recording ? "Started recording" : "Stopped recording");
            return true;
        }
        else {
            message.setValue("Unsupported control action for camera");
            return false;
        }
    }
}