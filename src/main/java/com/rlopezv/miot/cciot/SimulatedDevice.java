package com.rlopezv.miot.cciot;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.rlopezv.miot.cciot.util.PropertiesUtil;

public class SimulatedDevice {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceApp.class);
	private static final PropertiesUtil config = new PropertiesUtil();

	// The device connection string to authenticate the device with your IoT hub.
	// Using the Azure CLI:
	// az iot hub device-identity show-connection-string --hub-name {YourIoTHubName}
	// --device-id MyJavaDevice --output table
	private static String DEVICE_CONNECTION_STRING = "DEVICE_CONNECTION_STRING";
	private static final String DEVICE_ID = "DEVICE_ID";
	// Using the MQTT protocol to connect to IoT Hub
	private static IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
	private static DeviceClient client;

	// Print the acknowledgement received from IoT Hub for the telemetry message
	// sent.
	private static class EventCallback implements IotHubEventCallback {
		@Override
		public void execute(IotHubStatusCode status, Object context) {
			System.out.println("IoT Hub responded to message with status: " + status.name());

			if (context != null) {
				synchronized (context) {
					context.notify();
				}
			}
		}
	}

	private static class MessageSender implements Runnable {
		Object lockobj = new Object();
		@Override
		public void run() {
			try {
				// Initialize the simulated telemetry.
				double minTemperature = 20;
				double minHumidity = 60;
				Random rand = new Random();

				while (true) {
					// Simulate telemetry.
					double currentTemperature = minTemperature + rand.nextDouble() * 15;
					double currentHumidity = minHumidity + rand.nextDouble() * 20;
					TelemetryMessage telemetryMessage = new TelemetryMessage().withDeviceId(config.getProperty(DEVICE_ID, String.class)).withHumidity(currentHumidity).withTemperature(currentTemperature);

					// Add the telemetry to the message body as JSON.
					String msgStr = telemetryMessage.serialize();
					Message msg = new Message(msgStr);

					// Add a custom application property to the message.
					// An IoT hub can filter on these properties without access to the message body.
					msg.setProperty("temperatureAlert", currentTemperature > 30 ? "true" : "false");

					LOGGER.info("Sending message: {}", msgStr);

					// Send the message.
					EventCallback callback = new EventCallback();
					client.sendEventAsync(msg, callback, lockobj);

					Thread.sleep(10000);
				}
			} catch (InterruptedException | ConfigurationException e) {
				LOGGER.error("Finished",e);
			}
		}
	}

	public static void main(String[] args) throws Exception {

		// Connect to the IoT hub.
		client = new DeviceClient(config.getProperty(DEVICE_CONNECTION_STRING,String.class), protocol);
		client.open();

		// Create new thread and start sending messages
		MessageSender sender = new MessageSender();
		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.execute(sender);

		// Stop the application.
		System.out.println("Press ENTER to exit.");
		System.in.read();
		executor.shutdownNow();
		client.closeNow();
	}
}
