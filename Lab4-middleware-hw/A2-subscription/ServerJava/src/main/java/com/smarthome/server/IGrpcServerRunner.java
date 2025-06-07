package com.smarthome.server;

public interface IGrpcServerRunner {
    void start() throws Exception;
    void stop() throws Exception;
}