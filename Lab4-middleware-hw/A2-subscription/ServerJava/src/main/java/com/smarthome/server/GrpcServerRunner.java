package com.smarthome.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

public class GrpcServerRunner implements IGrpcServerRunner {
    private final int port;
    private final IDeviceManager deviceManager;
    private Server server;

    public GrpcServerRunner(int port, IDeviceManager deviceManager) {
        this.port = port;
        this.deviceManager = deviceManager;
    }

    @Override
    public void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(new SmarthomeServiceImpl(deviceManager))
                .build()
                .start();
        System.out.println("gRPC server listening on port " + port);
    }

    @Override
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination();
            System.out.println("gRPC server stopped");
        }
    }
}

