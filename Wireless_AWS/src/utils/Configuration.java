package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.yaml.snakeyaml.Yaml;

public class Configuration {

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	String processName;
	List<Node> nodes;
	HashMap<String, Group> groups;
	List<Rule> sendRules;
	List<Rule> receiveRules;
	HashMap<String, Object> parameters;

	Object config_data;
	String configFileAddress;
	String configLastModified;

	public Configuration(String name, String configFileAddress, int updateInterval, TimeUnit updateIntervalUnit) {
		this.processName = name;
		this.nodes = new ArrayList<Node>();
		this.groups = new HashMap<String, Group>();
		this.sendRules = new ArrayList<Rule>();
		this.receiveRules = new ArrayList<Rule>();
		this.parameters = new HashMap<String, Object>();
		this.configLastModified = null;
		this.configFileAddress = configFileAddress;
		// parse the config file

		try {
			loadConfigurations(loadYAML());
			update_config(updateInterval, updateIntervalUnit);
		} catch (IOException e) {
			System.out.println("failed to update config");
			e.printStackTrace();
		}
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

	@SuppressWarnings("unchecked")
	public void loadConfigurations(HashMap<String, Object> yamlData) {
		HashMap<String, Object> config = (HashMap<String, Object>) yamlData.get("configuration");
		synchronized (this.nodes) {
			this.nodes = Node.parseNodeMaps((List<HashMap<String, Object>>) config.get("nodes"));
		}
		if (config.containsKey("groups")) {
			parseGroups((List<HashMap<String, Object>>) config.get("groups"));
		}
		synchronized (this.parameters) {
			this.parameters = (HashMap<String, Object>) config.get("parameters");
		}

		updateRules(yamlData);
	}

	@SuppressWarnings("unchecked")
	private void updateRules(HashMap<String, Object> yamlData) {
		synchronized (this.sendRules) {
			this.sendRules = Rule.parseRuleMaps((List<HashMap<String, Object>>) yamlData.get("sendRules"));
		}
		synchronized (this.receiveRules) {
			this.receiveRules = Rule.parseRuleMaps((List<HashMap<String, Object>>) yamlData.get("receiveRules"));
		}
	}

	@SuppressWarnings("unchecked")
	private void parseGroups(List<HashMap<String, Object>> groups) {
		if (groups == null || groups.isEmpty()) {
			return;
		}

		// iterate over groups to parse
		for (HashMap<String, Object> group : groups) {
			String groupName = (String) group.get("name");
			if (groupName == null || groupName.isEmpty()) {
				continue;
			}

			List<String> members = (List<String>) group.get("members");
			if (members == null || members.isEmpty()) {
				continue;
			}

			// find existing member nodes to add to the group
			List<Node> groupNodes = new ArrayList<Node>(members.size());
			for (String memberName : members) {
				Node node = findNodeWithName(memberName);
				if (node != null) {
					groupNodes.add(node);
				}
			}
			// instantiate a group instance
			Group multicastGroup;
			try {
				multicastGroup = new Group(groupNodes, groupName, this.processName);
			} catch (Exception e) {
				System.out.println("Not in group: " + groupName);
				continue;
			}

			// add group to this.groups
			if (!groupNodes.isEmpty()) {
				synchronized (this.groups) {
					this.groups.put(groupName, multicastGroup);
				}
			}
		}
	}

	private Node findNodeWithName(String name) {
		if (name == null || name.isEmpty()) {
			return null;
		}
		synchronized (this.nodes) {
			for (Node node : this.nodes) {
				if (node.name.equals(name)) {
					return node;
				}
			}
		}
		return null;
	}

	private void update_config(int updateInterval, TimeUnit updateIntervalUnit) throws IOException {
		Runnable update = new Runnable() {
			public void run() {
				String last_modified = null;
				if (configFileAddress.contains("http://")) {
					URL u = null;
					try {
						u = new URL(configFileAddress);
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					URLConnection uc = null;
					try {
						uc = u.openConnection();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					uc.setUseCaches(false);
					last_modified = String.valueOf(uc.getLastModified());
				} else {
					File file = new File(configFileAddress);
					Path filePath = file.toPath();
					BasicFileAttributes attr = null;
					try {
						attr = Files.readAttributes(filePath, BasicFileAttributes.class);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					last_modified = attr.lastModifiedTime().toString();
				}

				if (!last_modified.equals(configLastModified)) {
					updateRules(loadYAML());
					configLastModified = last_modified;
				}
			}
		};
		// update the config file every 'time_interval'
		this.scheduler.scheduleAtFixedRate(update, 0, updateInterval, updateIntervalUnit);
	}

	public synchronized Node getNode(String name) {
		synchronized (this.nodes) {
			for (Node node : this.nodes) {
				if (node.name.equals(name)) {
					return node;
				}
			}
		}
		return null;
	}

	public synchronized int getNodeIndex(String name) {
		synchronized (this.nodes) {
			Node node;
			for (int i = 0; i < this.nodes.size(); i++) {
				node = this.nodes.get(i);
				if (node.name.equals(name)) {
					return i;
				}
			}
		}
		return -1;
	}

	/* Setters and Getters */

	public String getProcessName() {
		synchronized (this.processName) {
			return this.processName;
		}
	}

	public List<Node> getNodes() {
		List<Node> nodesList;
		synchronized (this.nodes) {
			nodesList = new ArrayList<Node>(this.nodes);
		}
		return nodesList;
	}

	public Group getGroup(String groupName) {
		if (groupName == null || groupName.isEmpty()) {
			return null;
		}

		Group group = null;
		synchronized (this.groups) {
			group = (Group) this.groups.get(groupName);
		}
		return group;
	}

	public HashMap<String, Group> getGroups() {
		HashMap<String, Group> groupsHashMap = null;
		synchronized (this.groups) {
			groupsHashMap = new HashMap<String, Group>(this.groups);
		}
		return groupsHashMap;
	}

	public synchronized List<Rule> getSendRules() {
		List<Rule> sendRules;
		synchronized (this.sendRules) {
			sendRules = new ArrayList<Rule>(this.sendRules);
		}
		return sendRules;
	}

	public synchronized List<Rule> getReceiveRules() {
		List<Rule> receiveRules;
		synchronized (this.receiveRules) {
			receiveRules = new ArrayList<Rule>(this.receiveRules);
		}
		return receiveRules;
	}

	public synchronized HashMap<String, Object> getParameters() {
		HashMap<String, Object> params;
		synchronized (this.parameters) {
			params = new HashMap<String, Object>(this.parameters);
		}
		return params;
	}

	public Object getParameter(String key) {
		Object param;
		synchronized (this.parameters) {
			param = this.parameters.get(key);
		}
		return param;
	}
}
