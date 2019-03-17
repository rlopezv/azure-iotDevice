package com.rlopezv.miot.cciot;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;

public class SimulatedDevice {

	// The device connection string to authenticate the device with your IoT hub.
	// Using the Azure CLI:
	// az iot hub device-identity show-connection-string --hub-name {YourIoTHubName}
	// --device-id MyJavaDevice --output table
	private static String CONNECTION_STRING = "HostName=IoTHub-rlv.azure-devices.net;DeviceId=rlv-java-device;SharedAccessKey=CbSE9ews5/7AouPx8B7C/w==";

	// Using the MQTT protocol to connect to IoT Hub
	private static IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
	private static DeviceClient client;

	// Specify the telemetry to send to your IoT hub.
	private static class TelemetryDataPoint {
		public double temperature;
		public double humidity;

		// Serialize object to JSON format.
		public String serialize() {
			Gson gson = new Gson();
			return gson.toJson(this);
		}
	}

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
					TelemetryDataPoint telemetryDataPoint = new TelemetryDataPoint();
					telemetryDataPoint.temperature = currentTemperature;
					telemetryDataPoint.humidity = currentHumidity;

					// Add the telemetry to the message body as JSON.
					String msgStr = telemetryDataPoint.serialize();
					Message msg = new Message(msgStr);

					// Add a custom application property to the message.
					// An IoT hub can filter on these properties without access to the message body.
					msg.setProperty("temperatureAlert", currentTemperature > 30 ? "true" : "false");

					System.out.println("Sending message: " + msgStr);

					Object lockobj = new Object();

					// Send the message.
					EventCallback callback = new EventCallback();
					client.sendEventAsync(msg, callback, lockobj);

					synchronized (lockobj) {
						lockobj.wait();
					}
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				System.out.println("Finished.");
			}
		}
	}

	public static void main(String[] args) throws IOException, URISyntaxException {

		// Connect to the IoT hub.
		client = new DeviceClient(CONNECTION_STRING, protocol);
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
