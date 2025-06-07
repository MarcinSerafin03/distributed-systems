import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public static void main(String[] args) {
        // Configure basic logging
        setupLogging();

        int port = 50051;
        String serverName = "server1";

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                logger.warning("Invalid port number provided, using default: " + port);
            }

            if (args.length > 1) {
                serverName = args[1];
            }
        }

        logger.info(String.format("Starting server %s on port %d", serverName, port));

        try {
            // Create device manager
            IDeviceManager deviceManager = new DeviceManager(serverName);

            // Create gRPC server runner
            IGrpcServerRunner grpcServerRunner = new GrpcServerRunner(port, deviceManager);

            // Create and start the hosted service
            GrpcServerHostedService service = new GrpcServerHostedService(grpcServerRunner);
            service.start();

            // Keep the application running until terminated
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Server failed to start", e);
        }
    }

    private static void setupLogging() {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);

        // Remove existing handlers
        for (java.util.logging.Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        // Add console handler
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        consoleHandler.setFormatter(new SimpleFormatter());
        rootLogger.addHandler(consoleHandler);
    }
}