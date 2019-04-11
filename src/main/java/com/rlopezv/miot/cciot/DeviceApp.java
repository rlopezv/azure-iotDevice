package com.rlopezv.miot.cciot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.iot.service.exceptions.IotHubException;
import com.microsoft.azure.iot.service.sdk.Device;
import com.microsoft.azure.iot.service.sdk.RegistryManager;
import com.rlopezv.miot.cciot.util.PropertiesUtil;

/**
 * Hello world!
 *
 */
public class DeviceApp
{

	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceApp.class);
	private static final String IOT_HUB_CONNECTION_STRING = "IOT_HUB_CONNECTION_STRING";
	private static final String DEVICE_ID = "DEVICE_ID";

	private static final PropertiesUtil config = new PropertiesUtil();
	
	public static void main( String[] args ) throws Exception
	{

		String deviceId = config.getProperty(DEVICE_ID, String.class);
		RegistryManager registryManager = RegistryManager.createFromConnectionString(config.getProperty(IOT_HUB_CONNECTION_STRING,String.class));

		Device device = Device.createFromId(deviceId, null, null);
		try {
			device = registryManager.addDevice(device);
		} catch (IotHubException iote) {
			try {
				device = registryManager.getDevice(deviceId);
			} catch (IotHubException iotf) {
				iotf.printStackTrace();
			}
		}
		LOGGER.info("Device id {}", device.getDeviceId());
		LOGGER.info("Device Key {}", device.getPrimaryKey());
	}
}
