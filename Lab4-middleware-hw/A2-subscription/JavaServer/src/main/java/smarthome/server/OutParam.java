package smarthome.server;

public class OutParam<T> {
    private T value;

    public OutParam() {
        this.value = null;
    }

    public OutParam(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}