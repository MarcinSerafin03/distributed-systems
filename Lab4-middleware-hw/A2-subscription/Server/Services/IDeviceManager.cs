namespace SmartHome.Server
{
    public interface IDeviceManager
    {
        string ServerName { get; }
        List<DeviceBase> Devices { get; }
        DeviceBase GetDeviceById(string deviceId);
        DeviceBase GetDeviceByName(string deviceName);
        List<DeviceBase> GetDevicesbyType(DeviceType deviceType);
        bool TryControlDevice(ControlRequest request, out string message);
    }
}