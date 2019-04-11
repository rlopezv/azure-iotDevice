/**
 * 
 */
package com.rlopezv.miot.cciot;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;

/**
 * Bean for telemetry
 * @author ramon
 *
 */
public class TelemetryMessage {

		public double temperature;
		public double humidity;
		public String deviceId;
		public String deviceTime = ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT );
		public int messageId;

		
		
		
		public double getTemperature() {
			return temperature;
		}



		public void setTemperature(double temperature) {
			this.temperature = temperature;
		}



		public double getHumidity() {
			return humidity;
		}



		public void setHumidity(double humidity) {
			this.humidity = humidity;
		}



		public String getDeviceId() {
			return deviceId;
		}



		public void setDeviceId(String deviceId) {
			this.deviceId = deviceId;
		}



		public String getDeviceTime() {
			return deviceTime;
		}



		public void setDeviceTime(String deviceTime) {
			this.deviceTime = deviceTime;
		}



		public int getMessageId() {
			return messageId;
		}



		public void setMessageId(int messageId) {
			this.messageId = messageId;
		}


		public TelemetryMessage withMessageId(int messageId) {
			this.messageId = messageId;
			return this;
		}

		public TelemetryMessage withDeviceTime(String deviceTime) {
			this.deviceTime = deviceTime;
			return this;
		}
		
		public TelemetryMessage withDeviceId(String deviceId) {
			this.deviceId = deviceId;
			return this;
		}
		
		public TelemetryMessage withTemperature(double temperature) {
			this.temperature = temperature;
			return this;
		}
		
		public TelemetryMessage withHumidity(double humidity) {
			this.humidity = humidity;
			return this;
		}
		
		// Serialize object to JSON format.
		public String serialize() {
			Gson gson = new Gson();
			return gson.toJson(this);
		}
}
