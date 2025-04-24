using System;
using SmartHome.Services;

namespace SmartHome.Server
{
    public class Refrigerator : DeviceBase
    {
        public RefrigeratorInfo.Types.Mode Mode { get; set; }
        public float CurrentTemperature { get; set; }
        public bool DoorOpen { get; set; }
        public List<RefrigeratorCompartment> Compartments { get; set; } = new List<RefrigeratorCompartment>();


        private readonly Random _random = new Random();

        public override DeviceInfoResponse GetDeviceInfo()
        {
            SimulateTemperatureFluctuations();
            
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
                Refrigerator = CreateRefrigeratorInfo()
            };
        }

        public override DeviceStatus GetDeviceStatus(string timestamp)
        {
            SimulateTemperatureFluctuations();
            
            return new DeviceStatus
            {
                DeviceId = Id,
                DeviceType = Type,
                IsOnline = IsOnline,
                Timestamp = timestamp,
                Refrigerator = CreateRefrigeratorInfo()
            };
        }

        private RefrigeratorInfo CreateRefrigeratorInfo(){
            var info = new RefrigeratorInfo
            {
                Mode = Mode,
                CurrentTemperature = CurrentTemperature,
                DoorOpen = DoorOpen
            };
            foreach (var compartment in Compartments)
            {
                info.Compartments.Add(new Services.Compartment
                {
                    Name = compartment.Name,
                    CurrentTemperature = compartment.CurrentTemperature,
                    TargetTemperature = compartment.TargetTemperature
                });
            }
            
            return info;
        }
        private void SimulateTemperatureFluctuations()
        {
            // Simulate temperature fluctuations
            CurrentTemperature += (float)(_random.NextDouble() * 2 - 1); // Random change between -1 and +1
            foreach (var compartment in Compartments)
            {
                var diff = compartment.TargetTemperature - compartment.CurrentTemperature;
                var adjustment = (float)(diff * 0.1 + (_random.NextDouble() * 0.2 - 0.1));
                compartment.CurrentTemperature += adjustment;
            }
        }

        public override bool TryHandleControlRequest(ControlRequest request, out string message)
        {
            if (request.ControlActionCase == ControlRequest.ControlActionOneofCase.SetTemperature)
            {
                var action = request.SetTemperature;
                var compartment = Compartments.FirstOrDefault(c => c.Name == action.CompartmentName);
                
                if (compartment == null)
                {
                    message = $"Compartment '{action.CompartmentName}' not found";
                    return false;
                }
                
                compartment.TargetTemperature = action.Temperature;
                message = $"Set target temperature of {action.CompartmentName} to {action.Temperature}°C";
                return true;
            }
            else if (request.ControlActionCase == ControlRequest.ControlActionOneofCase.SetRefrigeratorMode)
            {
                var action = request.SetRefrigeratorMode;
                CurrentMode = action.Mode;
                message = $"Set refrigerator mode to {action.Mode}";
                return true;
            }
            else
            {
                message = "Unsupported control action for refrigerator";
                return false;
            }
        }
    }
}