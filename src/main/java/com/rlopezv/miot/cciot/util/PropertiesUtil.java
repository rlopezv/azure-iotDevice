/**
 * 
 */
package com.rlopezv.miot.cciot.util;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * Class for reading proprties using apache commons
 * Lazy initilization
 * @author ramon
 *
 */
public class PropertiesUtil {

	private String fileName = "azure.properties";
	private Configuration config ;
	
	public PropertiesUtil() {
		
	}
	

	public PropertiesUtil(String fileName) {
		this.fileName = fileName;
	}

	protected Configuration init() throws ConfigurationException {
		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
		    new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
		    .configure(params.properties()
		        .setFileName(fileName));
		    return builder.getConfiguration();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getProperty (String key, Class<T> clazz) throws ConfigurationException {
		Object value = null;
		if (clazz.isAssignableFrom(String.class)) {
			value = getConfig().getString(key);
		}
		return (T) value;
	}

	@SuppressWarnings("unchecked")
	public <T> T getProperty (String key, Class<T> clazz, T defaulValue) throws ConfigurationException {
		Object value = getProperty(key,clazz);
		if (value==null) {
			value = defaulValue;
		}
		return (T) value;
	}
	
	public Configuration getConfig() throws ConfigurationException {
		if (config==null) {
			config = init();
		}
		return config;
	}

	
}
