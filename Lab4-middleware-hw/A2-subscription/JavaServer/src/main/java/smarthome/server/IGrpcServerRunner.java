package smarthome.server;

public interface IGrpcServerRunner {
    void startAsync() throws Exception;
    void stopAsync() throws Exception;
}

