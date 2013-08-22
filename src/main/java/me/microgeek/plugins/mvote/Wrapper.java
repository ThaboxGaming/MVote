package me.microgeek.plugins.mvote;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.microgeek.plugins.mvote.vote.PlayerChecker;

public class Wrapper {

	public static MVote instance;
	public static PlayerChecker pc = new PlayerChecker();
	public static final Map<Integer, List<String>> EXPIRY_QUEUE = new HashMap<Integer, List<String>>();
	
}
