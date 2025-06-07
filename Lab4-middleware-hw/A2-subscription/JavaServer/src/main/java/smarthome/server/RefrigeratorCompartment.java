package smarthome.server;

public class RefrigeratorCompartment {
    private String name;
    private float targetTemperature;
    private float currentTemperature;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getTargetTemperature() {
        return targetTemperature;
    }

    public void setTargetTemperature(float targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    public float getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(float currentTemperature) {
        this.currentTemperature = currentTemperature;
    }
}