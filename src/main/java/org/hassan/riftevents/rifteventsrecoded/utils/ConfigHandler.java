package org.hassan.riftevents.rifteventsrecoded.utils;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ConfigHandler {

	private static ConfigHandler instance;
	private final JavaPlugin plugin;

	public ConfigHandler(JavaPlugin plugin) {
		this.plugin = plugin;
		instance = this;
		createConfigs();
	}

	private void createConfigs() {
		for (Configs config : Configs.values()) {
			config.init(this);
		}
	}

	public FileConfiguration createConfig(String name) {
		File conf = new File(plugin.getDataFolder(), name);

		if (!conf.exists()) {
			conf.getParentFile().mkdirs();
			plugin.saveResource(name, false);
		}

		FileConfiguration configRet = new YamlConfiguration();

		try {
			configRet.load(conf);
			return configRet;
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void saveConfig(FileConfiguration config, String name) {
		try {
			config.save(new File(plugin.getDataFolder(), name));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public enum Configs {
		ARENAS("arena.yml");

		private String name;
		private FileConfiguration config;

		Configs(String name) {
			this.name = name;
		}

		public void init(ConfigHandler handler) {
			this.config = handler.createConfig(name);
		}

		public FileConfiguration getConfig() {
			return config;
		}

		public void saveConfig() {
			instance.saveConfig(config, name);
		}
	}
}
