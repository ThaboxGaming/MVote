package me.microgeek.plugins.mvote.util.config;

import java.util.LinkedHashSet;
import java.util.Set;


public class ConfigUtilities {

	public static final Set<Config> CONFIG_LIST = new LinkedHashSet<Config>();

	/**
	 * Get a Config by name
	 * @param name The name of the target Config Object
	 * @return An instance of Config, or a new one if it does not exist
	 */
	public static Config getConfigByName(String name) {
		for(Config c : CONFIG_LIST) {
			if(c.getName().equalsIgnoreCase(name)) {
				return c;
			}
		}
		return new Config(name);
	}

	/**
	 * Saves all loaded Configs
	 */
	public static void saveConfigs() {
		for(Config c : CONFIG_LIST) {
			if(c.isSave()) {
				c.saveConfig();
			}
		}
	}
}
