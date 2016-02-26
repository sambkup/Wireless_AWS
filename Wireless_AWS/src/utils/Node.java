package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Node {
	public String name = null;
	public String ip = null;
	public int port = 0;
	public String address = null;

	public Node(HashMap<String, Object> node) {
		if (node == null) {
			return;
		}

		this.name = (String) node.get("name");
		this.ip = (String) node.get("ip");
		this.port = (int) node.get("port");
		this.address = String.format("/%s:%d", this.ip, this.port);
	}

	public static List<Node> parseNodeMaps(List<HashMap<String, Object>> nodeMaps) {
		List<Node> nodes = new ArrayList<Node>(nodeMaps.size());
		for (HashMap<String, Object> nodeMap : nodeMaps) {
			nodes.add(new Node(nodeMap));
		}
		return nodes;
	}

	public String toString() {
		return String.format("%s(%s:%d)", name, ip, port);
	}
}
