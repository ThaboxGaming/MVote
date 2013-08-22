package me.microgeek.plugins.mvote.util.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import me.microgeek.plugins.mvote.Wrapper;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {

	private String name;
	private FileConfiguration config;
	private File location;
	private boolean save = true;

	public Config(String name) {
		this.name = name;
		saveDefaultConfig();
		saveConfig();
		ConfigUtilities.CONFIG_LIST.add(this);
	}

	/**
	 * Clears the config
	 */
	public void clear() {
		String[] sections = config.getConfigurationSection("").getKeys(false).toArray(new String[0]);
		for(String s : sections){
			config.set(s, null);
		}
	}

	/**
	 * Saves the default config
	 */
	public void saveDefaultConfig() {
	    if (location == null) {
	    	location = new File(Wrapper.instance.getDataFolder(), name + ".yml");
	    }
	    if (!location.exists()) {            
	         Wrapper.instance.saveResource(name + ".yml", false);
	     }
	}
	
	/**
	 * Get the configs name
	 * @return name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Save the config
	 */
	public void saveConfig() {
		if (this.config == null || this.location == null) {
			reloadConfig();
		}
		try {
			getConfig().save(this.location);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the configs FileConfiguration
	 * @return the FileConfiguration
	 */
	public FileConfiguration getConfig() {
		if (this.config == null) {
			reloadConfig();
		}
		return this.config;
	}

	/**
	 * Reload the config
	 */
	public void reloadConfig() {
		this.location = new File(Wrapper.instance.getDataFolder(), this.name + ".yml");
		this.config = YamlConfiguration.loadConfiguration(location);
		InputStream defConfigStream = Wrapper.instance.getResource(name + ".yml");
		if (defConfigStream != null) {
			YamlConfiguration yc = YamlConfiguration.loadConfiguration(defConfigStream);
			config.setDefaults(yc);
			saveConfig();
		}
	}

	/**
	 * @return the save
	 */
	public boolean isSave() {
		return save;
	}

	/**
	 * @param save the save to set
	 */
	public void setSave(boolean save) {
		this.save = save;
	}

	/**
	 * @return the location
	 */
	public File getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(File location) {
		this.location = location;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param config the config to set
	 */
	public void setConfig(FileConfiguration config) {
		this.config = config;
	}

}
