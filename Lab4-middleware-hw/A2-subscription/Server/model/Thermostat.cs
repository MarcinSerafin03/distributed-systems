using System;
using SmartHome.Services;

namespace SmartHome.Server
{
    public class Thermostat: DeviceBase
    {
        public string TemperatureUnit { get; set; }
        public float CurrentTemperature { get; set; }
        public float TargetTemperature { get; set; }
        public string Location { get; set; }
        public int BatteryLevel { get; set; }

        private readonly Random _random = new Random();

        public override DeviceInfoResponse GetDeviceInfo()
        {
            return new DeviceInfoResponse
            {
                Device = new Device
                {
                    Id = Id,
                    Name = Name,
                    Type = Type,
                    SubType = SubType,
                    IsOnline = IsOnline, 
                },
                Thermostat = new ThermostatInfo
                {
                    TemperatureUnit = TemperatureUnit,
                    CurrentTemperature = CurrentTemperature,
                    TargetTemperature = TargetTemperature,
                    Location = Location,
                    BatteryLevel = BatteryLevel
                }
            };
        }

        public override DeviceStatus GetDeviceStatus(string timestamp)
        {
            CurrentTemperature = (float)(_random.NextDouble() * 30); // Simulate current temperature
            TargetTemperature = (float)(_random.NextDouble() * 30); // Simulate target temperature
            return new DeviceStatus
            {
                DeviceId = Id,
                DeviceType = Type,
                IsOnline = IsOnline,
                Timestamp = timestamp,
                Thermostat = new ThermostatInfo
                {
                    TemperatureUnit = TemperatureUnit,
                    CurrentTemperature = CurrentTemperature,
                    TargetTemperature = TargetTemperature,
                    Location = Location,
                    BatteryLevel = BatteryLevel
                },
                
            };
        }

        public override bool TryHandleControlRequest(ControlRequest request, out string message)
        {
            message = string.Empty;
            if (request.Thermostat != null)
            {
                if (request.Thermostat.TargetTemperature.HasValue)
                {
                    TargetTemperature = request.Thermostat.TargetTemperature.Value;
                    message = $"Target temperature set to {TargetTemperature}°{TemperatureUnit}";
                }
                else
                {
                    message = "No target temperature provided.";
                    return false;
                }
            }
            return true;
        }
    }
}