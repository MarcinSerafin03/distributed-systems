package smarthome.server;

import smarthome.services.DeviceType;
import smarthome.services.DeviceInfoResponse;
import smarthome.services.DeviceStatus;
import smarthome.services.ControlRequest;

public abstract class DeviceBase {
    private String id;
    private String name;
    private DeviceType type;
    private String subType;
    private boolean isOnline;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public abstract DeviceInfoResponse getDeviceInfo();
    public abstract DeviceStatus getDeviceStatus(String timestamp);
    public abstract boolean tryHandleControlRequest(ControlRequest request, OutParam<String> message);
}