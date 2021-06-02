package ingenious.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigsLoader {
	
	private static ConfigsLoader configLoader;
	String filepath;
	String graphdb;
	
	private ConfigsLoader(){}

	public static ConfigsLoader getInstance(){
		if (configLoader == null) {
			configLoader = new ConfigsLoader();
		}

		return configLoader;
	}
	
	
	/**
	* Function that loads the config.properties file.
	*/
	public void  loadProperties() {
		// Load properties file
		Properties properties = new Properties();
		String configPath = "/config.properties";

		InputStream is = ConfigsLoader.class.getResourceAsStream(configPath);
		try {
			properties.load(is);
		} catch (IOException e) {

				System.err.println("Property file not found: " + e.getMessage());
		}

		// Read properties
		graphdb = properties.getProperty("graphdb");
		filepath = properties.getProperty("filepath");
		
		System.out.println("graphdb: " + graphdb + ", filepath: " + filepath);
	}
	
	public String getFilepath() {
		return filepath;
	}
	
	public String getGraphdb() {
		return graphdb;
	}
}
