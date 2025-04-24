using System;
using SmartHome.Services;

namespace SmartHome.Server
{
    public class SecurityCamera: DeviceBase
    {
        public string Location { get; set; }
        public bool Recording { get; set; }
        public Postion Position { get; set; }
        public int BatteryLevel { get; set; }

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
                SecurityCamera = new SecurityCameraInfo
                {
                    Location = Location,
                    Recording = Recording,
                    Position = Position,
                    BatteryLevel = BatteryLevel
                }
            };
        }

        public override DeviceStatus GetDeviceStatus(string timestamp)
        {
            if (Recording && BatteryLevel < 100)
            {
                BatteryLevel = Math.Max(0, BatteryLevel - 1);
            }
            return new DeviceStatus
            {
                DeviceId = Id,
                DeviceType = Type,
                IsOnline = IsOnline,
                Timestamp = timestamp,
                SecurityCamera = new SecurityCameraInfo
                {
                    Location = Location,
                    Recording = Recording,
                    Position = Position,
                    BatteryLevel = BatteryLevel
                }
            };
        }

        public override bool TryHandleControlRequest(ControlRequest request, out string message)
        {
            if (request.ControlActionCase == ControlRequest.ControlActionOneofCase.SetPtzPosition)
            {
                if (Subtype != "PTZ")
                {
                    message = "This camera does not support PTZ control";
                    return false;
                }
                
                var action = request.SetPtzPosition;
                PtzPosition = new PtzPosition
                {
                    Pan = Math.Clamp(action.Position.Pan, 0, 360),
                    Tilt = Math.Clamp(action.Position.Tilt, 0, 90),
                    Zoom = Math.Clamp(action.Position.Zoom, 1, 10)
                };
                
                message = $"Set camera position to Pan={PtzPosition.Pan}, Tilt={PtzPosition.Tilt}, Zoom={PtzPosition.Zoom}";
                return true;
            }
            else if (request.ControlActionCase == ControlRequest.ControlActionOneofCase.SetRecordingState)
            {
                var action = request.SetRecordingState;
                Recording = action.Recording;
                message = Recording ? "Started recording" : "Stopped recording";
                return true;
            }
            else
            {
                message = "Unsupported control action for camera";
                return false;
            }
        }
    }
}