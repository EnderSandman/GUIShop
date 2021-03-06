package com.pablo67340.shop.main;

import java.io.*;
import java.util.*;


import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;


import com.pablo67340.shop.handler.*;
import com.pablo67340.shop.listener.PlayerListener;

import de.dustplanet.util.SilkUtil;

public final class Main extends JavaPlugin {

	public File defaultConfigFile;


	private static Economy ECONOMY;

	/**
	 * An instance of this class.
	 */
	public static Main INSTANCE;

	/**
	 * A {@link Set} that will store every command that can
	 * be used by a {@link Player} to open the {@link Menu}.
	 */
	public static final Set<String> BUY_COMMANDS = new HashSet<>();

	/**
	 * A {@link Set} that will store every command that can
	 * be used by a {@link Player} to open the {@link Sell}
	 * GUI.
	 */
	public static final Set<String> SELL_COMMANDS = new HashSet<>();

	/**
	 * A {@link Set} that holds the names of each {@link Player}
	 * that currently has the {@link Menu} open.
	 */
	public static final Set<String> HAS_MENU_OPEN = new HashSet<>();

	/**
	 * A {@link Set} that holds the names of each {@link Player}
	 * that currently has the {@link Sell} GUI open.
	 */
	public static final Set<String> HAS_SELL_OPEN = new HashSet<>();

	/**
	 * A {@link Map} that holds the names of each {@link Player}
	 * that currently has a {@link Shop} open as well as the shop
	 * that is open.
	 */
	public static final Map<String, Shop> HAS_SHOP_OPEN = new HashMap<>();

	/**
	 * A {@link Map} that will store our {@link Menu}s
	 * when the server first starts.
	 * 
	 * @key
	 * 		The name of the {@link Player}.
	 * @value
	 * 		The menu.
	 */
	public static final Map<String, Menu> MENUS = new HashMap<>();

	/**
	 * A {@link Map} that will store our {@link Creator}s
	 * when the server first starts.
	 * 
	 * @key
	 * 		The name of the {@link Player}.
	 * @value
	 * 		The creator.
	 */
	public static final Map<String, Creator> CREATOR = new HashMap<>();

	/**
	 * A {@link Map} that will store our {@link Sell}s
	 * when the server first starts.
	 * 
	 * @key
	 * 		The name of the {@link Player}.
	 * @value
	 * 		The sell menu.
	 */
	public static final Map<String, Sell> SELLS = new HashMap<>();

	/**
	 * A {@link Map} that will store our {@link Shop}s
	 * when the server first starts.
	 * 
	 * @key
	 * 		The index on the {@link Menu} that this shop
	 * 		is located at.
	 * @value
	 * 		The shop.
	 */
	public static final Map<Integer, Shop> SHOPS = new HashMap<>();

	/**
	 * A {@link Map} that holds the prices to buy and sell
	 * an {@link Item} to/from a {@link Shop}.
	 * 
	 * @key
	 * 		The item's ID.
	 * @value
	 * 		The item's price object.
	 */
	public static final Map<String, Price> PRICES = new HashMap<>();

	public SilkUtil su;
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		createFiles();
		if (setupEconomy()){
			if (setupSilk()){
				su = SilkUtil.hookIntoSilkSpanwers();
				if (updateConfig()){
					getServer().getPluginManager().registerEvents(PlayerListener.INSTANCE, this);
					loadDefaults();
					Shop.loadShops();
				}
			}else{
				pluginError("SilkSpawners");
			}
		}else{
			pluginError("Vault");
		}
	}

	public void pluginError(String input){
		getLogger().warning(input+" was not installed! This plugin is required!");
		getServer().getPluginManager().disablePlugin(this);
	}

	/**
	 * 
	 * Check if Vault, SilkSpawners is enabled
	 */
	public Boolean setupSilk(){
		if (getServer().getPluginManager().getPlugin("SilkSpawners") == null){
			return false;
		}else{
			return true;
		}
	}
	
	
	private Boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}

		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

		if (rsp == null) {
			return false;
		}

		ECONOMY = (Economy) rsp.getProvider();

		if (ECONOMY == null ) {
			pluginError("An economy plugin");
			return false;
		}
		return true;
	}

	/**
	 * 
	 * Check if the config is up to date.
	 */
	@SuppressWarnings("unused")
	public Boolean updateConfig(){
		Double ver = getMainConfig().getDouble("ver");
		if (ver != null){
			if (ver == 1.0){
				getLogger().warning("The config version is outdated! Automatically updating config...");
				getMainConfig().set("menu-cols", null);
				getMainConfig().set("ver", 1.1);
				getLogger().warning("Config update successful!");
				saveMainConfig();
				return true;
			}else if (ver == 1.1){
				getLogger().info("Config all up to date!");
				return true;
			}else{
				getLogger().warning("The config version is outdated! Please delete your config.yml and restart!");
				getServer().getPluginManager().disablePlugin(this);
				return false;
			}
		}else{
			getLogger().warning("The config version is outdated! Please delete your config.yml and restart!");
			getServer().getPluginManager().disablePlugin(this);
			return false;
		}
	}

	/**
	 * 
	 * Load all deault config values, translate colors, store.
	 */
	public void loadDefaults() {
		BUY_COMMANDS.addAll(getMainConfig().getStringList("buy-commands"));
		SELL_COMMANDS.addAll(getMainConfig().getStringList("sell-commands"));
		Utils.setPrefix(ChatColor.translateAlternateColorCodes('&', getMainConfig().getString("prefix")));
		Utils.setSignsOnly(getMainConfig().getBoolean("signs-only"));
		Utils.setSignTitle(ChatColor.translateAlternateColorCodes('&', getMainConfig().getString("sign-title")));
		Utils.setNotEnoughPre(ChatColor.translateAlternateColorCodes('&', getMainConfig().getString("not-enough-pre")));
		Utils.setNotEnoughPost(ChatColor.translateAlternateColorCodes('&', getMainConfig().getString("not-enough-post")));
		Utils.setPurchased(ChatColor.translateAlternateColorCodes('&', getMainConfig().getString("purchased")));
		Utils.setTaken(ChatColor.translateAlternateColorCodes('&', getMainConfig().getString("taken")));
		Utils.setSold(ChatColor.translateAlternateColorCodes('&', getMainConfig().getString("sold")));
		Utils.setAdded(ChatColor.translateAlternateColorCodes('&', getMainConfig().getString("added")));
		Utils.setCantSell(ChatColor.translateAlternateColorCodes('&', getMainConfig().getString("cant-sell")));
		Utils.setEscapeOnly(getMainConfig().getBoolean("escape-only"));
		Utils.setSound(getMainConfig().getString("purchase-sound"));
		Utils.setSoundEnabled(getMainConfig().getBoolean("enable-sound"));
		Utils.setCreatorEnabled(getMainConfig().getBoolean("ingame-config"));
		Utils.setCantBuy(ChatColor.translateAlternateColorCodes('&', getMainConfig().getString("cant-buy")));
		Utils.setMenuRows(getMainConfig().getInt("menu-rows"));
		getDataFolder();
	}


	public File configf, specialf;
	private FileConfiguration config, special;


	/**
	 * 
	 * Get the CustomConfigFile (Shops)
	 */
	public FileConfiguration getCustomConfig() {
		return this.special;
	}

	/**
	 * 
	 * Get main ConfigFile (Config)
	 */
	public FileConfiguration getMainConfig() {
		return this.config;
	}
	
	public void saveMainConfig(){
		try {
			getMainConfig().save(configf);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Force create all YML files.
	 */
	public void createFiles() {

		configf = new File(getDataFolder(), "config.yml");
		specialf = new File(getDataFolder(), "shops.yml");

		if (!configf.exists()) {
			configf.getParentFile().mkdirs();
			saveResource("config.yml", false);
		}

		if (!specialf.exists()) {
			specialf.getParentFile().mkdirs();
			saveResource("shops.yml", false);
		}

		config = new YamlConfiguration();
		special = new YamlConfiguration();
		try {
			try {
				config.load(configf);
				special.load(specialf);
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	



	/**
	 * 
	 * Get the current economy object
	 */
	public static Economy getEconomy() {
		return ECONOMY;
	}

	/**
	 * 
	 * Get the Main Instance of GUIShop
	 */
	public static Main getInstance(){
		return INSTANCE;
	}

}
