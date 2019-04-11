package com.rlopezv.miot.cciot;

import java.net.URI;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventHubRuntimeInformation;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.rlopezv.miot.cciot.util.PropertiesUtil;

public class ReadDeviceToCloudMessages {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReadDeviceToCloudMessages.class);
	private static final PropertiesUtil config = new PropertiesUtil();

	// az iot hub show --query properties.eventHubEndpoints.events.endpoint --name {your IoT Hub name}
	private static final String EVENTHUB_COMTAPIBLE_ENPOINT = "EVENTHUB_COMTAPIBLE_ENPOINT";

	// az iot hub show --query properties.eventHubEndpoints.events.path --name {your IoT Hub name}
	private static final String EVENTHUB_COMPATIBLE_PATH = "EVENTHUB_COMPATIBLE_PATH";

	// az iot hub policy show --name iothubowner --query primaryKey --hub-name {your IoT Hub name}
	private static final String IOT_HUB_SAS_KEY = "IOT_HUB_SAS_KEY";
	private static final String IOT_HUB_SAS_KEY_NAME = "IOT_HUB_SAS_KEY_NAME";

	// Track all the PartitionReciever instances created.
	private static ArrayList<PartitionReceiver> receivers = new ArrayList<PartitionReceiver>();

	// Asynchronously create a PartitionReceiver for a partition and then start
	// reading any messages sent from the simulated client.
	private static void receiveMessages(EventHubClient ehClient, String partitionId)
			throws EventHubException, ExecutionException, InterruptedException {

		final ExecutorService executorService = Executors.newSingleThreadExecutor();

		// Create the receiver using the default consumer group.
		// For the purposes of this sample, read only messages sent since
		// the time the receiver is created. Typically, you don't want to skip any messages.
		ehClient.createReceiver(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, partitionId,
				EventPosition.fromEnqueuedTime(Instant.now())).thenAcceptAsync(receiver -> {
					LOGGER.info("Starting receive loop on partition:{}", partitionId);
					LOGGER.info("Reading messages sent since: %s", Instant.now().toString());

					receivers.add(receiver);

					while (true) {
						try {
							// Check for EventData - this methods times out if there is nothing to retrieve.
							Iterable<EventData> receivedEvents = receiver.receiveSync(100);

							// If there is data in the batch, process it.
							if (receivedEvents != null) {
								for (EventData receivedEvent : receivedEvents) {
									LOGGER.info("Telemetry received:\n {}",
											new String(receivedEvent.getBytes(), Charset.defaultCharset()));
									LOGGER.info("Application properties (set by device):\n{}",receivedEvent.getProperties().toString());
									LOGGER.info("System properties (set by IoT Hub):\n{}\n",receivedEvent.getSystemProperties().toString());
								}
							}
						} catch (EventHubException e) {
							LOGGER.error("Error reading EventData",e);
						}
					}
				}, executorService);
	}

	public static void main(String[] args)
			throws EventHubException, Exception {

		final ConnectionStringBuilder connStr = new ConnectionStringBuilder()
				.setEndpoint(new URI(config.getProperty(EVENTHUB_COMTAPIBLE_ENPOINT, String.class)))
				.setEventHubName(config.getProperty(EVENTHUB_COMPATIBLE_PATH, String.class))
				.setSasKeyName(config.getProperty(IOT_HUB_SAS_KEY_NAME,String.class))
				.setSasKey(config.getProperty(IOT_HUB_SAS_KEY,String.class));

		// Create an EventHubClient instance to connect to the
		// IoT Hub Event Hubs-compatible endpoint.
		final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
		final EventHubClient ehClient = EventHubClient.createSync(connStr.toString(), executorService);

		// Use the EventHubRunTimeInformation to find out how many partitions
		// there are on the hub.
		final EventHubRuntimeInformation eventHubInfo = ehClient.getRuntimeInformation().get();

		// Create a PartitionReciever for each partition on the hub.
		for (String partitionId : eventHubInfo.getPartitionIds()) {
			receiveMessages(ehClient, partitionId);
		}

		// Shut down cleanly.
		System.out.println("Press ENTER to exit.");
		System.in.read();
		System.out.println("Shutting down...");
		for (PartitionReceiver receiver : receivers) {
			receiver.closeSync();
		}
		ehClient.closeSync();
		executorService.shutdown();
		System.exit(0);
	}
}