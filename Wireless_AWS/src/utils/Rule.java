package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Rule {
	public enum RuleActions {
		none, drop, dropAfter, delay
	}

	public RuleActions action = RuleActions.none;
	public String src = null;
	public String dest = null;
	public String kind = null;
	public int seqNum = -1; // -1 meaning invalid or null

	public Rule(HashMap<String, Object> rule) {
		if (rule == null) {
			return;
		}

		this.action = stringToAction((String) rule.get("action"));
		this.src = (String) rule.get("src");
		this.dest = (String) rule.get("dest");
		this.kind = (String) rule.get("kind");
		this.seqNum = (int) rule.getOrDefault("seqNum", -1);
	}

	public static List<Rule> parseRuleMaps(List<HashMap<String, Object>> ruleMaps) {
		List<Rule> rules = new ArrayList<Rule>(ruleMaps.size());
		for (HashMap<String, Object> ruleMap : ruleMaps) {
			rules.add(new Rule(ruleMap));
		}
		return rules;
	}

	public static RuleActions stringToAction(String action) {
		if (action == null || action.isEmpty()) {
			return RuleActions.none;
		}
		if (action.equals("drop")) {
			return RuleActions.drop;
		} else if (action.equals("dropAfter")) {
			return RuleActions.dropAfter;
		} else if (action.equals("delay")) {
			return RuleActions.delay;
		}
		return RuleActions.none;
	}

	public static String actionToString(RuleActions act) {
		switch (act) {
		case delay:
			return "delay";
		case drop:
			return "drop";
		case dropAfter:
			return "drop after";
		default:
			return "none";
		}
	}

	public String toString() {
		return String.format("Action: %s, Src: %s, Dest: %s, Kind: %s, SeqNum: %d", actionToString(this.action),
				this.src, this.kind, this.seqNum);
	}
}
