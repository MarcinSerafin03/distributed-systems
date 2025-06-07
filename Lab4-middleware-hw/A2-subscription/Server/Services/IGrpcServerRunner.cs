namespace SmartHome.Server
{
    public interface IGrcpServerRunner
    {
        Task StartAsync(CancellationToken cancellationToken);
        Task StopAsync(CancellationToken cancellationToken);
    }
}