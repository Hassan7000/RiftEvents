package org.hassan.riftevents.rifteventsrecoded.games;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.hassan.riftevents.rifteventsrecoded.RiftEvents;
import org.hassan.riftevents.rifteventsrecoded.games.gameobject.Game;
import org.hassan.riftevents.rifteventsrecoded.utils.Common;

import java.util.*;
import java.util.stream.Collectors;

public class Dropper extends Game implements Listener {

	private HashMap<UUID, Integer> points = new HashMap<>();
	private HashMap<UUID, Boolean> hasBeenInWater = new HashMap<>();
	public Dropper() {
		super("Dropper");
		Bukkit.getPluginManager().registerEvents(this, RiftEvents.getInstance());
	}

	@Override
	public void began() {
		if(getGamePlayers().size() < getMinPlayers()){
			stop(true);
			Common.broadcastMessage("&bThe Dropper gamemode has been stopped!");
			Bukkit.getScheduler().cancelTask(getTaskID());
			return;
		}

		Bukkit.getScheduler().cancelTask(getTaskID());
		setCurrentStatus(Status.BEGAN);

		Location location = getRandomLocation();
		teleportGamePlayers(location);
		sendGameMessage("GAME HAS STARTED");

	}


	@Override
	public void stop(boolean message) {
		for(UUID uuid : getGamePlayers()){
			Player player = Bukkit.getPlayer(uuid);
			player.teleport(getLobbySpawn());
			player.sendMessage("Dropper game mode has been stopped");
		}

		getGamePlayers().clear();
		for(UUID uuid : getSpectators()){
			Player player = Bukkit.getPlayer(uuid);
			player.getInventory().clear();
			player.teleport(getLobbySpawn());
			player.sendMessage("Dropper game mode has been stopped");
		}
		getSpectators().clear();
		setCurrentStatus(Status.OFFLINE);
		points.clear();
	}



	@Override
	public void checkWin() {
		boolean hasWon = false;
		for(UUID uuid : getGamePlayers()){
			Player player = Bukkit.getPlayer(uuid);
			if(player == null) continue;
			if(getPlayerPoint(player) >= 6){
				sendVictory(player);
				hasWon = true;
				break;
			}
		}
		if(hasWon){
			teleportGamePlayers(getLobbySpawn());
			getGamePlayers().clear();
			getSpectators().clear();
			points.clear();
			hasBeenInWater.clear();
			Bukkit.getScheduler().cancelTask(getTaskID());
			setCurrentStatus(Status.OFFLINE);
		}

	}

	@Override
	public void leave(Player player) {
		if(getGamePlayers().contains(player.getUniqueId())){
			getGamePlayers().remove(player.getUniqueId());
			player.teleport(getLobbySpawn());
			checkWin();
			player.sendMessage("You have left the " + getName() + " event");
		}
		if(getSpectators().contains(player.getUniqueId())){
			getSpectators().remove(player.getUniqueId());
			player.teleport(getLobbySpawn());
			checkWin();
			player.sendMessage("You have left the " + getName() + " event");
		}
	}

	private void handlePointMessage(Player player){
		if (getCurrentStatus() == Status.BEGAN && getGamePlayers().contains(player.getUniqueId())) {
			Comparator<Map.Entry<UUID, Integer>> comp = Comparator.<Map.Entry<UUID, Integer>>comparingInt(Map.Entry::getValue).thenComparing(e -> getPlayerPoint(Bukkit.getPlayer(e.getKey()))).reversed();
			List<Map.Entry<UUID, Integer>> sorted = points.entrySet().stream().sorted(comp).collect(Collectors.toList());
			UUID uuid = player.getUniqueId();
			int points = 0;
			for (int i = 0; i < sorted.size(); i++) {
				Map.Entry<UUID, Integer> e = sorted.get(i);
				uuid = e.getKey();
				points = e.getValue();
				break;
			}
			sendGameMessage(Common.colorMessage("&5Dropper &8>>&f" + Bukkit.getPlayer(uuid).getName() + " &dis in the lead with &d" + points + "/6"));
		}

	}

	private void handlePoint(int playerPoints, Player player){
		points.put(player.getUniqueId(),getPlayerPoint(player) + 1);
		Common.sendMessage(player,"&a+1");
		handlePointMessage(player);
	}

	@EventHandler
	public void onWaterHit(PlayerMoveEvent event){
		if (getCurrentStatus() == Status.BEGAN) {
			Player player = event.getPlayer();
			if(!getGamePlayers().contains(player.getUniqueId())) return;
				Block block = event.getFrom().getBlock();
				if (!(block.getType().name().contains("WATER") || block.getType().name().contains("STATIONARY_WATER"))) {
					return;
				}

				if(hasBeenInWater.get(player.getUniqueId()) == false){
					int playerPoints = getPlayerPoint(player);

					Bukkit.getScheduler().runTaskLater(RiftEvents.getInstance(), () -> handlePoint(playerPoints, player), 15L);
					hasBeenInWater.put(player.getUniqueId(),true);

				}


				Bukkit.getScheduler().runTaskLater(RiftEvents.getInstance(), () -> hasBeenInWater.put(player.getUniqueId(),false), 1 * 20L);
				checkWin();

		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e){
		Player player = e.getPlayer();
		if(isPlaying(player)){
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onFall(EntityDamageEvent e) {
		if (getCurrentStatus() == Status.BEGAN) {
			if (e.getEntity() instanceof Player) {
				EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;
				Player p = (Player) e.getEntity();
				if (event != null && e.getEntity() instanceof Player && event.getDamager() instanceof Player) {
					e.setCancelled(true);
					return;
				}
				if (getGamePlayers().contains(p.getUniqueId())) {
					if(p.getWorld().getName().equalsIgnoreCase("Dropper")){
						e.setCancelled(true);
						p.setHealth(0.0);
						p.spigot().respawn();
						checkWin();
					}

				}
			}
		}
	}

	@EventHandler
	public void onFood(FoodLevelChangeEvent e) {
		Player player = (Player) e.getEntity();
		if (isPlaying(player)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void playerQuitEvent(PlayerQuitEvent e){
		Player player = e.getPlayer();
		if(getCurrentStatus() == Status.STARTING || getCurrentStatus() == Status.BEGAN){
			if(getGamePlayers().contains(player.getUniqueId())){
				getGamePlayers().remove(player.getUniqueId());
				rollbackPlayer(player);
				player.teleport(getLobbySpawn());
				sendGameMessage(player.getName() + " has left the game");
				checkWin();
			}else{
				if(getSpectators().contains(player.getUniqueId())){
					getSpectators().remove(player.getUniqueId());
					rollbackPlayer(player);
					player.teleport(getLobbySpawn());
					sendGameMessage(player.getName() + " has left the game");
					checkWin();
				}
			}
		}

	}

	@Override
	public void preGame() {
		setMinPlayers(RiftEvents.getInstance().getConfig().getInt("Games." + getName() + ".Min-Players"));
		setMaxPlayers(RiftEvents.getInstance().getConfig().getInt("Games." + getName() + ".Max-Players"));
		setStartTime(RiftEvents.getInstance().getConfig().getInt("Games." + getName() + ".starting-time"));
	}

	@Override
	public void postJoin(Player player) {
		player.teleport(getLobbySpawn());
		points.put(player.getUniqueId(),0);
		hasBeenInWater.put(player.getUniqueId(),false);
	}


	public int getPlayerPoint(Player player){
		return points.get(player.getUniqueId()) != null ? points.get(player.getUniqueId()) : 0;
	}

	private static Dropper instance;
	public static Dropper getInstance(){
		if(instance == null){
			instance = new Dropper();
		}
		return instance;
	}
}
