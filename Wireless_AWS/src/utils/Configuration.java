package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import org.yaml.snakeyaml.Yaml;

public class Configuration {

	String serverIP;
	int serverPort;
	
	String configFileAddress;
	String configLastModified;

	public Configuration(String configFileAddress) {
		this.configLastModified = null;
		this.configFileAddress = configFileAddress;
		// parse the config file

		loadConfigurations(loadYAML());

	}

	@SuppressWarnings("unchecked")
	private HashMap<String, Object> loadYAML() {

		// download/import file
		Yaml yaml = new Yaml();
		InputStream input = null;
		HashMap<String, Object> yaml_data = null;
		try {
			if (this.configFileAddress.contains("http://")) {
				input = new URL(this.configFileAddress).openStream();
			} else {
				input = new FileInputStream(new File(configFileAddress));
			}

			// load file
			yaml_data = (HashMap<String, Object>) yaml.load(input);
			// System.out.println(yaml_data.toString());
			if (input != null)
				input.close();
			return (HashMap<String, Object>) yaml_data;
		} catch (MalformedURLException e) {
			System.out.println("Couldn't load URL.");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't find the file.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Couldn't open/close file.");
			e.printStackTrace();
		}

		return null;
	}

	public void loadConfigurations(HashMap<String, Object> yamlData) {
		this.serverIP = (String) yamlData.get("ip");
		this.serverPort = (int) yamlData.get("port");
	}
	
	/* Setters and Getters */
	

	/**
	 * @return the serverIP
	 */
	public String getServerIP() {
		return serverIP;
	}

	/**
	 * @param serverIP the serverIP to set
	 */
	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}

	/**
	 * @return the serverPort
	 */
	public int getServerPort() {
		return serverPort;
	}

	/**
	 * @param serverPort the serverPort to set
	 */
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}	

}
