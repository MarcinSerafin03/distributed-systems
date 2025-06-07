package smarthome.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import smarthome.services.SmartHomeServiceGrpc;

public class GrpcServerRunner implements IGrpcServerRunner {
    private static final Logger logger = Logger.getLogger(GrpcServerRunner.class.getName());

    private final int port;
    private final IDeviceManager deviceManager;
    private Server server;

    public GrpcServerRunner(int port, IDeviceManager deviceManager) {
        this.port = port;
        this.deviceManager = deviceManager;
    }

    @Override
    public void startAsync() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(new SmartHomeServiceImpl(deviceManager))
                .build()
                .start();

        logger.info("gRPC server listening on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    GrpcServerRunner.this.stopAsync();
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        });
    }

    @Override
    public void stopAsync() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            logger.info("gRPC server stopped");
        }
    }
}