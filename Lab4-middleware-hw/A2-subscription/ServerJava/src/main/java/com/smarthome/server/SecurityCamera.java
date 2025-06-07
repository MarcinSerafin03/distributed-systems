package com.smarthome.server;

import com.smarthome.services.SmartHome.*;
import java.util.Random;

public class SecurityCamera extends DeviceBase {
    private String location;
    private boolean recording;
    private Position position;
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

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
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
        Device device = Device.newBuilder()
                .setId(id)
                .setName(name)
                .setType(type)
                .setSubType(subType)
                .setOnline(isOnline)
                .build();

        SecurityCameraInfo cameraInfo = SecurityCameraInfo.newBuilder()
                .setLocation(location)
                .setRecording(recording)
                .setPosition(position)
                .setBatteryLevel(batteryLevel)
                .build();

        return DeviceInfoResponse.newBuilder()
                .setDevice(device)
                .setSecurityCameraInfo(cameraInfo)
                .build();
    }

    @Override
    public DeviceStatus getDeviceStatus(String timestamp) {
        if (recording && batteryLevel < 100) {
            batteryLevel = Math.max(0, batteryLevel - 1);
        }

        SecurityCameraInfo cameraInfo = SecurityCameraInfo.newBuilder()
                .setLocation(location)
                .setRecording(recording)
                .setPosition(position)
                .setBatteryLevel(batteryLevel)
                .build();

        return DeviceStatus.newBuilder()
                .setDeviceId(id)
                .setDeviceType(type)
                .setIsOnline(isOnline)
                .setSecurityCameraInfo(cameraInfo)
                .build();
    }

    @Override
    public boolean tryHandleControlRequest(ControlRequest request, MessageHolder messageHolder) {
        if (request.getControlCommandCase() == ControlRequest.ControlCommandCase.SET_POSITION) {
            if (!subType.equals("PTZ")) {
                messageHolder.setMessage("This camera does not support PTZ control");
                return false;
            }

            SetPosition action = request.getSetPosition();
            Position.Builder posBuilder = Position.newBuilder();
            posBuilder.setPan(Math.min(Math.max(action.getPosition().getPan(), 0), 360));
            posBuilder.setTilt(Math.min(Math.max(action.getPosition().getTilt(), 0), 90));
            posBuilder.setZoom(Math.min(Math.max(action.getPosition().getZoom(), 1), 10));
            position = posBuilder.build();

            messageHolder.setMessage(String.format("Set camera position to Pan=%s, Tilt=%s, Zoom=%s",
                    position.getPan(), position.getTilt(), position.getZoom()));
            return true;
        }
        else if (request.getControlCommandCase() == ControlRequest.ControlCommandCase.SET_RECORDING) {
            SetRecording action = request.getSetRecording();
            recording = action.getRecording();
            messageHolder.setMessage(recording ? "Started recording" : "Stopped recording");
            return true;
        }
        else {
            messageHolder.setMessage("Unsupported control action for camera");
            return false;
        }
    }
}