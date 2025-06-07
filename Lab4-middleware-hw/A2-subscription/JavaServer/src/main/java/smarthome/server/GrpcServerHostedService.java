package smarthome.server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GrpcServerHostedService {
    private static final Logger logger = Logger.getLogger(GrpcServerHostedService.class.getName());

    private final IGrpcServerRunner grpcServerRunner;

    public GrpcServerHostedService(IGrpcServerRunner grpcServerRunner) {
        this.grpcServerRunner = grpcServerRunner;
    }

    public void start() {
        try {
            grpcServerRunner.startAsync();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to start gRPC server", e);
        }
    }

    public void stop() {
        try {
            grpcServerRunner.stopAsync();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to stop gRPC server", e);
        }
    }
}