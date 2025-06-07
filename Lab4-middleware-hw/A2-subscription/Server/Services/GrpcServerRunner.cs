namespace SmartHome.Server
{
    public class GrpcServerRunner : IGrcpServerRunner
    {
        private readonly int _port;
        private readonly IDeviceManager _deviceManager;
        private Server _server;

        public GrpcServerRunner(int port, IDeviceManager deviceManager)
        {
            _port = port;
            _deviceManager = deviceManager;
        }

        public Task StartAsync(CancellationToken cancellationToken)
        {
            _server = new Server
            {
                Services = { SmartHomeService.BindService(new SmartHomeServiceImpl(_deviceManager)) },
                Ports = { new ServerPort("0.0.0.0", _port, ServerCredentials.Insecure) }
            };
            _server.Start();
            Console.WriteLine($"gRPC server listening on port {_port}");
            return Task.CompletedTask;
        }

        public Task StopAsync(CancellationToken cancellationToken)
        {
            _server.ShutdownAsync().Wait(cancellationToken);
            Console.WriteLine("gRPC server stopped");
            return Task.CompletedTask;
        }
    }
    }