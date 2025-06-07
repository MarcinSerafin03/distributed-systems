namespace SmartHome.Server
{
    public class GrpcServerHostedService : IHostedService
    {
        private readonly IGrcpServerRunner _grpcServerRunner;

        public GrpcServerHostedService(IGrcpServerRunner grpcServerRunner)
        {
            _grpcServerRunner = grpcServerRunner;
        }

        public Task StartAsync(CancellationToken cancellationToken)
        {
            return _grpcServerRunner.StartAsync(cancellationToken);
        }

        public Task StopAsync(CancellationToken cancellationToken)
        {
            return _grpcServerRunner.StopAsync(cancellationToken);
        }
    }
}