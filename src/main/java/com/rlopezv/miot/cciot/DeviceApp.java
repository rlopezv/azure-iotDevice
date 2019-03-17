package com.rlopezv.miot.cciot;

import com.microsoft.azure.iot.service.exceptions.IotHubException;
import com.microsoft.azure.iot.service.sdk.Device;
import com.microsoft.azure.iot.service.sdk.RegistryManager;

/**
 * Hello world!
 *
 */
public class DeviceApp
{

	private static final String CONNECTION_STRING = "HostName=IoTHub-rlv.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=Pp/0AdlXDDOaelMLyYq4mLtk+PrmHmDdSve2LXUDFv4=";
	private static final String DEVICE_ID = "rlv-java-device";

	public static void main( String[] args ) throws Exception
	{


		RegistryManager registryManager = RegistryManager.createFromConnectionString(CONNECTION_STRING);

		Device device = Device.createFromId(DEVICE_ID, null, null);
		try {
			device = registryManager.addDevice(device);
		} catch (IotHubException iote) {
			try {
				device = registryManager.getDevice(DEVICE_ID);
			} catch (IotHubException iotf) {
				iotf.printStackTrace();
			}
		}
		System.out.println("Device id: " + device.getDeviceId());
		System.out.println("Device key: " + device.getPrimaryKey());
	}
}
