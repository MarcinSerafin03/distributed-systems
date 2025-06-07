package com.smarthome.server;

import com.smarthome.services.*;
import com.smarthome.services.SmartHome.*;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

public class SmarthomeServiceImpl extends SmartHomeServiceGrpc.SmartHomeServiceImplBase {
    private final IDeviceManager deviceManager;

    public SmarthomeServiceImpl(IDeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    @Override
    public void listDevices(ListDevicesRequest request, StreamObserver<ListDevicesResponse> responseObserver) {
        ListDevicesResponse.Builder responseBuilder = ListDevicesResponse.newBuilder();

        for (DeviceBase device : deviceManager.getDevices()) {
            Device.Builder deviceBuilder = Device.newBuilder()
                    .setId(device.getId())
                    .setName(device.getName())
                    .setType(device.getType())
                    .setSubType(device.getSubType())
                    .setOnline(device.isOnline());

            responseBuilder.addDevices(deviceBuilder.build());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getDeviceInfo(DeviceInfoRequest request, StreamObserver<DeviceInfoResponse> responseObserver) {
        DeviceBase device = deviceManager.getDeviceById(request.getDeviceId());

        if (device != null) {
            responseObserver.onNext(device.getDeviceInfo());
        } else {
            // Send empty response if device not found
            responseObserver.onNext(DeviceInfoResponse.newBuilder().build());
        }

        responseObserver.onCompleted();
    }

    @Override
    public void controlDevice(ControlRequest request, StreamObserver<ControlResponse> responseObserver) {
        MessageHolder messageHolder = new MessageHolder();
        boolean success = deviceManager.tryControlDevice(request, messageHolder);

        ControlResponse response = ControlResponse.newBuilder()
                .setDeviceId(request.getDeviceId())
                .setSuccess(success)
                .setMessage(messageHolder.getMessage())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void monitorDevice(MonitorRequest request, StreamObserver<DeviceStatus> responseObserver) {
        DeviceBase device = deviceManager.getDeviceById(request.getDeviceId());

        if (device == null) {
            responseObserver.onCompleted();
            return;
        }

        // Start a separate thread to send periodic updates
        new Thread(() -> {
            try {
                int interval = Math.max(1, request.getInterval());

                // Send 10 updates and then complete
                for (int i = 0; i < 10 && !Thread.currentThread().isInterrupted(); i++) {
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    DeviceStatus status = device.getDeviceStatus(timestamp);
                    responseObserver.onNext(status);

                    try {
                        TimeUnit.SECONDS.sleep(interval);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                responseObserver.onCompleted();
            }
        }).start();
    }
}
