package org.hassan.riftevents.rifteventsrecoded.games.gameobject;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.hassan.riftevents.rifteventsrecoded.RiftEvents;
import org.hassan.riftevents.rifteventsrecoded.games.Spleef;
import org.hassan.riftevents.rifteventsrecoded.utils.Common;
import org.hassan.riftevents.rifteventsrecoded.utils.ImageChar;
import org.hassan.riftevents.rifteventsrecoded.utils.ImageMessage;
import org.hassan.riftevents.rifteventsrecoded.utils.ItemBuilder;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.*;

public abstract class Game implements Listener {


	private String name;

	public String getName() { return name;}
	public void setName(String name){
		this.name = name;
	}


	private String displayName;

	public String getDisplayName;
	public String getDisplayName(){return displayName;}
	public void setDisplayName(String displayName){
		this.displayName = displayName;
	}




	private ArrayList<UUID> countDownState = new ArrayList<>();
	public ArrayList<UUID> getCountDownState(){ return countDownState;}
	public void addCountDownState(Player player){
		getCountDownState().add(player.getUniqueId());
	}


	private final ArrayList<UUID> gamePlayers = new ArrayList<>();
	public ArrayList<UUID> getGamePlayers() { return gamePlayers; }
	public void addGamePlayer(Player player){
		getGamePlayers().add(player.getUniqueId());
	}
	public void removeGamePlayer(Player player){
		getGamePlayers().remove(player.getUniqueId());
	}

	private int countDown;
	public int getCountDown(){
		return countDown;
	}
	public void setCountDown(int countDown){
		this.countDown = countDown;
	}


	private final ArrayList<UUID> spectators = new ArrayList<>();
	public ArrayList<UUID> getSpectators() { return spectators;}


	private long startTime;
	public long getStartTime(){return startTime;}
	public void setStartTime(long startTime){
		this.startTime = startTime;
	}


	private int maxPlayers;
	public int getMaxPlayers(){return maxPlayers;}
	public void setMaxPlayers(int maxPlayers){
		this.maxPlayers = maxPlayers;
	}


	private int minPlayers;
	public int getMinPlayers(){ return minPlayers;}
	public void setMinPlayers(int minPlayers){
		this.minPlayers = minPlayers;
	}

	private int taskID;
	public int getTaskID(){return taskID;}
	public void setTaskID(int taskID){
		this.taskID = taskID;
	}

	private boolean cancelHitting = false;
	public void setCancelHitting(boolean cancelHitting){
		this.cancelHitting = cancelHitting;
	}

	public enum Status {
		STARTING,
		BEGAN,
		OFFLINE
	}


	private Status currentStatus;
	public Status getCurrentStatus(){ return currentStatus;}
	public void setCurrentStatus(Status currentStatus){
		this.currentStatus = currentStatus;
	}

	private boolean isSpectatorEnabled;
	public boolean isSpectatorEnabled(){
		return isSpectatorEnabled;
	}
	public void setSpectatorEnabled(boolean isSpectatorEnabled){
		this.isSpectatorEnabled = isSpectatorEnabled;
	}

	public Game(String name) {
		this.name = name;
	}


	public void start() {
		preGame();
		setCurrentStatus(Status.STARTING);
		sendImage(getName());
		if(getName().equalsIgnoreCase("Spleef")){
			Spleef.getInstance().findLocations();
		}
		BukkitTask start = new BukkitRunnable() {
			@Override
			public void run() {

				TextComponent message = new TextComponent(Common.colorMessage(RiftEvents.getInstance().getConfig().getString("broadcasts.broadcast").replace("%player%", "RiftMc").replace("%event%", getName()).replace("%timeleft%", String.valueOf(getStartTime()))));
				TextComponent click = new TextComponent(Common.colorMessage(RiftEvents.getInstance().getConfig().getString("broadcasts.started-click")));
				message.addExtra(click);

				click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Common.colorMessage(RiftEvents.getInstance().getConfig().getString("broadcasts.started-hover"))).create()));
				click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/events join " + getName()));

				int timeLeft = (int) getStartTime();
				List<String> counts = RiftEvents.getInstance().getConfig().getStringList("Games." + getName() + ".broadcast-times");
				if (counts.contains(Long.toString(getStartTime()))) {
					Bukkit.getServer().spigot().broadcast(message);
				}
				timeLeft--;
				setStartTime(timeLeft);

				if (getStartTime() <= 0) {
					began();
				}
			}
		}.runTaskTimer(RiftEvents.getInstance(), 60L, 20L);
		setTaskID(start.getTaskId());
	}

	public void startCountDown(int seconds){
		setCountDown(seconds);
		BukkitTask task = new BukkitRunnable(){
			@Override
			public void run() {
				int timeLeft = getCountDown();
				sendCountDownMessage(timeLeft);
				timeLeft--;
				setCountDown(timeLeft);
				if(getCountDown() <= 0){
					cancel();
					getCountDownState().clear();
					setCancelHitting(false);
				}
			}
		}.runTaskTimer(RiftEvents.getInstance(), 60L, 20L);
	}



	public abstract void began();

	public abstract void stop(boolean message);

//	public abstract void join(Player player, boolean silent);

	public abstract void checkWin();

	public abstract void leave(Player player);

	public abstract void preGame();
	public abstract void postJoin(Player player);

	public void join(Player player, boolean silent) {
		if(getGamePlayers().contains(player.getUniqueId())){
			player.sendMessage("You are already in the event");
			return;
		}
		if(getGamePlayers().size() > getMaxPlayers()){
			player.sendMessage("MAX PLAYERS REACHED");
			return;
		}

		addGamePlayer(player);
		if(!silent){
			Bukkit.broadcastMessage(player.getName() + " has joined the dropper gamemode");
		}

		for(String joinMessage : RiftEvents.getInstance().getConfig().getStringList("Games.broadcast-on-join-event")){
			Common.broadcastMessage(joinMessage);
		}
		postJoin(player);
	}


	public void clearPotions(Player player){
		for (PotionEffect types : player.getActivePotionEffects()) {
			player.removePotionEffect(types.getType());
		}
	}

	public void addItemToGamePlayers(ItemStack item){
		for(UUID uuid : getGamePlayers()){
			Player player = Bukkit.getPlayer(uuid);
			if(player != null){
				player.getInventory().addItem(item);
			}
		}
	}

	public void rollbackPlayer(Player players) {

	}

	public void sendCountDownMessage(int timeLeft){
		for(UUID uuid : getCountDownState()){
			Player player = Bukkit.getPlayer(uuid);
			if(player != null){
				Common.sendMessage(player,"&b"+timeLeft + " &7seconds left before fight!");
			}
		}
	}

	public void setupGame(Player player, String event){
		ItemStack arena = new ItemBuilder(Material.GRASS)
				.setDisplayName(Common.colorMessage("&aSet Arena spawn for "+ event))
				.setKey("arenaspawn", event)
				.build();
		player.getInventory().addItem(arena);

		ItemStack lobby = new ItemBuilder(Material.NOTE_BLOCK)
				.setDisplayName(Common.colorMessage("&cSet lobby spawn for " + event))
				.setKey("lobbyspawn",event)
				.build();
		player.getInventory().addItem(lobby);

		ItemStack spectator = new ItemBuilder(Material.MOB_SPAWNER)
				.setDisplayName(Common.colorMessage("&6Set Spectator spawn for " + event))
				.setKey("spectatorspawn",event)
				.build();
		player.getInventory().addItem(spectator);

		ItemStack random = new ItemBuilder(Material.WOOL)
				.setDisplayName(Common.colorMessage("&bSet random location for " + event))
				.setKey("randomlocation",event)
				.build();
		player.getInventory().addItem(random);

		ItemStack firstSpawn = new ItemBuilder(Material.DIAMOND_BLOCK)
				.setDisplayName(Common.colorMessage("&4Set the first spawn location"))
				.setKey("firstspawn",event)
				.build();
		player.getInventory().addItem(firstSpawn);

		ItemStack secondSpawn = new ItemBuilder(Material.EMERALD_BLOCK)
				.setDisplayName(Common.colorMessage("&dSet the second spawn location"))
				.setKey("secondspawn",event)
				.build();
		player.getInventory().addItem(secondSpawn);

		ItemStack setFirstPos = new ItemBuilder(Material.SNOW_BLOCK)
				.setDisplayName(Common.colorMessage("&aSet the first pos for " + event))
				.setKey("setFirstPos", event)
				.build();
		player.getInventory().addItem(setFirstPos);

		ItemStack setSecondPos = new ItemBuilder(Material.WOOD)
				.setDisplayName(Common.colorMessage("&aSet the second pos for " + event))
				.setKey("setSecondPos", event)
				.build();
		player.getInventory().addItem(setSecondPos);

		RiftEvents.getInstance().getSetupState().add(player.getUniqueId());
	}

	public Location getRandomLocation(){
		List<String> spawns = RiftEvents.getInstance().getArenas().getStringList("Games." + getName() + ".RandomLocations");
		return Common.deserializeL(spawns.get(new Random().nextInt(spawns.size())));
	}

	public Location getArenaSpawn(){
		return Common.deserializeL(RiftEvents.getInstance().getArenas().getString("Games." + getName() + ".ArenaSpawn"));
	}
	public Location getSpectatorSpawn(){
		return Common.deserializeL(RiftEvents.getInstance().getArenas().getString("Games." + getName() + ".SpectatorSpawn"));
	}

	public Location getFirstSpawn(){
		return Common.deserializeL(RiftEvents.getInstance().getArenas().getString("Games." + getName() + ".FirstSpawn"));
	}

	public Location getSecondSpawn(){
		return Common.deserializeL(RiftEvents.getInstance().getArenas().getString("Games." + getName() + ".SecondSpawn"));
	}

	public Location getFirstPos(){
		return Common.deserializeL(RiftEvents.getInstance().getArenas().getString("Games." + getName() + ".FirstPos"));
	}

	public Location getSecondPos(){
		return Common.deserializeL(RiftEvents.getInstance().getArenas().getString("Games." + getName() + ".SecondPos"));
	}

	public void setSpectator(Player player) {

	}

	public void removeSpectator(Player player) {

	}


	public Location getLobbySpawn(){
		return Common.deserializeL(RiftEvents.getInstance().getArenas().getString("GameWorld.LobbySpawn"));
	}

	public void sendGameMessage(String message){
		for(UUID uuid : getGamePlayers()){
			Player player = Bukkit.getPlayer(uuid);
			if(player == null) continue;
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',message));
		}
		for(UUID uuid : getSpectators()){
			Player player = Bukkit.getPlayer(uuid);
			if(player == null) continue;
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',message));
		}
	}

	public void teleportGamePlayers(Location location){
		Iterator<UUID> iterator = gamePlayers.listIterator();
		new BukkitRunnable() {
			public void run() {
				if (iterator.hasNext()) {
					UUID uuid  = iterator.next();
					if(uuid != null){
						Player player = Bukkit.getPlayer(uuid);
						if(player != null){
							player.teleport(location);
						}
					}

				}
			}
		}.runTaskTimer(RiftEvents.getInstance(), 2, 2);
	}

	public void teleportGamePlayer(Player player, Location location){
		player.teleport(location);
	}

	public boolean isPlaying(Player player){
		if(getCurrentStatus() == Status.BEGAN || getCurrentStatus() == Status.STARTING){
			if(getGamePlayers().contains(player.getUniqueId())){
				return true;
			}else{
				if(getSpectators().contains(player.getUniqueId())){
					return true;
				}
			}

		}
		return false;
	}



	@EventHandler
	public void onPlayerDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if(isPlaying(player)){
			event.getItemDrop().setItemStack(null);
		}
	}


	@EventHandler
	public void onItemPickUp(EntityPickupItemEvent e){
		if(e.getEntity() instanceof Player){
			Player player = (Player) e.getEntity();
			if(isPlaying(player)){
				e.getItem().setItemStack(null);
			}
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e){
		Player player = e.getPlayer();
		if(isPlaying(player)){
			e.setCancelled(true);
		}
	}


	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e){
		if(getCountDownState().contains(e.getPlayer().getUniqueId())){
			if (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) {
				e.setTo(e.getFrom());
			}
		}
	}

	@EventHandler
	public void onHit(EntityDamageEvent event) {
		if (getCurrentStatus() == Status.BEGAN || getCurrentStatus() == Status.STARTING) {
			if (event instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
				if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
					Player player = (Player) e.getEntity();
					Player damager = (Player) e.getDamager();

					if(cancelHitting){
						e.setCancelled(true);
					}

				}
			}
		}
	}


	@EventHandler
	public void Commands(PlayerCommandPreprocessEvent e) {
		Player player = e.getPlayer();
		if(isPlaying(player)){
			if (e.getMessage().startsWith("/") && !e.getPlayer().hasPermission("events.commands.bypass") && !e.getMessage().startsWith("/events")) {
				e.setCancelled(true);
			}
		}
	}

	public void sendImage(String name) {
		try {
			BufferedImage image = ImageIO.read(RiftEvents.getInstance().getResource("Images/" + name + ".png"));
			ImageMessage message = new ImageMessage(image, 9, ImageChar.BLOCK.getChar());
			message.appendTextToLines(3, Common.colorMessage(RiftEvents.getInstance().getConfig().getString("broadcasts.line-1").replace("%timeleft%", String.valueOf(getStartTime())).replace("%player%", "RiftMc").replace("%event%", getName())), Common.colorMessage(RiftEvents.getInstance().getConfig().getString("broadcasts.line-2").replace("%timeleft%", String.valueOf(getStartTime())).replace("%player%", "RiftMc").replace("%event%", getName())), Common.colorMessage(RiftEvents.getInstance().getConfig().getString("broadcasts.line-3").replace("%timeleft%", String.valueOf(getStartTime())).replace("%player%", "RiftMc").replace("%event%", getName())), Common.colorMessage(RiftEvents.getInstance().getConfig().getString("broadcasts.line-4").replace("%timeleft%", String.valueOf(getStartTime())).replace("%player%", "RiftMc").replace("%event%", getName())));
			message.sendToPlayer(getName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}



	public void sendVictory(Player p) {
		try {
			BufferedImage image = ImageIO.read(new URL("https://crafatar.com/avatars/" + p.getUniqueId() + ".png"));
			ImageMessage message = new ImageMessage(image, 8, ImageChar.BLOCK.getChar());
			message.appendTextToLines(3, Common.colorMessage(RiftEvents.getInstance().getConfig().getString("broadcasts.win-1").replace("%player%" , p.getName()).replace("%event%", getName())), Common.colorMessage(RiftEvents.getInstance().getConfig().getString("broadcasts.win-2").replace("%player%" , p.getName()).replace("%event%", getName())));
			message.sendToPlayer(getName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


}

