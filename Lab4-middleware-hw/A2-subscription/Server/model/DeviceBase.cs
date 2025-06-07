using System;
using SmartHome.Services;

namespace SmartHome.Server
{
    public abstract class DeviceBase
    {
        public string id { get; set; }
        public string Name { get; set; }
        public DeviceType Type { get; set; }
        public string subType { get; set; }
        public bool IsOnline { get; set; }

        public abstract DeviceInfoResponse GetDeviceInfo();
        public abstract DeviceStatus GetDeviceStatus(string timestamp);
        public abstract bool TryHandleControlRequest(ControlRequest request, out string Message);
    }
}