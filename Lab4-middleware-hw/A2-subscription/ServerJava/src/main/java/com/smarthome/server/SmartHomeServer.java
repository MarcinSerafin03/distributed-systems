package com.smarthome.server;

public class SmartHomeServer {
    public static void main(String[] args) {
        int port = 50051;
        String serverName = "server1";

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + args[0]);
            }

            if (args.length > 1) {
                serverName = args[1];
            }
        }

        System.out.println("Starting server " + serverName + " on port " + port);

        // Create device manager
        IDeviceManager deviceManager = new DeviceManager(serverName);

        // Create and start gRPC server
        IGrpcServerRunner serverRunner = new GrpcServerRunner(port, deviceManager);

        try {
            serverRunner.start();

            // Keep the server running until terminated
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down gRPC server due to JVM shutdown");
                try {
                    serverRunner.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));

            // Wait indefinitely
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
