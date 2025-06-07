package smarthome.server;

import io.grpc.stub.StreamObserver;
import smarthome.services.ControlRequest;
import smarthome.services.ControlResponse;
import smarthome.services.DeviceInfoRequest;
import smarthome.services.DeviceInfoResponse;
import smarthome.services.DeviceListRequest;
import smarthome.services.DeviceListResponse;
import smarthome.services.DeviceStatus;
import smarthome.services.DeviceStatusRequest;
import smarthome.services.DeviceStatusResponse;
import smarthome.services.SmartHomeServiceGrpc;
import java.util.List;

public class SmartHomeServiceImpl extends SmartHomeServiceGrpc.SmartHomeServiceImplBase {
    private final IDeviceManager deviceManager;

    public SmartHomeServiceImpl(IDeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    @Override
    public void getDeviceList(DeviceListRequest request, StreamObserver<DeviceListResponse> responseObserver) {
        DeviceListResponse.Builder responseBuilder = DeviceListResponse.newBuilder()
                .setServerName(deviceManager.getServerName());

        for (DeviceBase device : deviceManager.getDevices()) {
            responseBuilder.addDevices(device.getDeviceInfo().getDevice());
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
            // Return an empty response if device not found
            responseObserver.onNext(DeviceInfoResponse.newBuilder().build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getDeviceStatus(DeviceStatusRequest request, StreamObserver<DeviceStatusResponse> responseObserver) {
        DeviceStatusResponse.Builder responseBuilder = DeviceStatusResponse.newBuilder()
                .setServerName(deviceManager.getServerName())
                .setTimestamp(request.getTimestamp());

        if (request.getDeviceId().isEmpty()) {
            // Return all device statuses
            for (DeviceBase device : deviceManager.getDevices()) {
                responseBuilder.addStatuses(device.getDeviceStatus(request.getTimestamp()));
            }
        } else {
            // Return specific device status
            DeviceBase device = deviceManager.getDeviceById(request.getDeviceId());
            if (device != null) {
                responseBuilder.addStatuses(device.getDeviceStatus(request.getTimestamp()));
            }
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void controlDevice(ControlRequest request, StreamObserver<ControlResponse> responseObserver) {
        OutParam<String> message = new OutParam<>("");
        boolean success = deviceManager.tryControlDevice(request, message);

        ControlResponse response = ControlResponse.newBuilder()
                .setSuccess(success)
                .setMessage(message.getValue())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}