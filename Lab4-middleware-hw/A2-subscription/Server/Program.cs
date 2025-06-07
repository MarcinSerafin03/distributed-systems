using System;
using System.Threading.Tasks;
using Grpc.Core;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using SmartHome.Services;

namespace SmartHome.Server
{
    public class Program
    {
        public static async Task Main(string[] args)
        {
            int port = 50051;
            string serverName = "server1";
            
            if (args.Length > 0)
            {
                if (!int.TryParse(args[0], out int parsedPort))
                {
                    port = parsedPort;
                }
                if (args.Length > 1)
                {
                    serverName = args[1];
                }
            }
            
            Console.WriteLine($"Starting server {serverName} on port {port}");
            CreateHostBuilder(args,port,serverName).Build().Run();
        }
        
        public static IHostBuilder CreateHostBuilder(string[] args, int port, string serverName) =>
            Host.CreateDefaultBuilder(args)
                .ConfigureServices((hostContext, services) =>
                {
                    services.AddSingleton<IDeviceManager>(new DeviceManager(serverName));

                    services.AddGrpc();
                    
                    services.AddSingleton<IGrcpServerRunner>(provider =>
                        new GrpcServerRunner(port, provider.GetRequiredService<IDeviceManager>()));
                    services.AddHostedService<GrpcServerHostedService>();
                })
                .ConfigureLogging(logging =>
                {
                    logging.ClearProviders();
                    logging.AddConsole();
                });
    }
}