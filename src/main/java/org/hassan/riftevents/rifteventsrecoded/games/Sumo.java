package org.hassan.riftevents.rifteventsrecoded.games;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.hassan.riftevents.rifteventsrecoded.RiftEvents;
import org.hassan.riftevents.rifteventsrecoded.games.gameobject.Game;
import org.hassan.riftevents.rifteventsrecoded.utils.Common;
import org.hassan.riftevents.rifteventsrecoded.utils.ItemBuilder;

import java.util.*;
import java.util.logging.Level;

public class Sumo extends Game implements Listener {

	private HashMap<Integer, UUID> fighting = new HashMap<>();
	private ArrayList<UUID> isFighting = new ArrayList<>();
	public Sumo() {
		super("Sumo");
		Bukkit.getPluginManager().registerEvents(this, RiftEvents.getInstance());
	}

	@Override
	public void began() {
		if(getGamePlayers().size() < getMinPlayers() || isFighting.size() > 0){
			stop(true);
			Common.broadcastMessage("&bThe Dropper gamemode has been stopped!");
			Bukkit.getScheduler().cancelTask(getTaskID());
			preGame();
			return;
		}

		Bukkit.getScheduler().cancelTask(getTaskID());
		teleportGamePlayers(getSpectatorSpawn());
		setCurrentStatus(Status.BEGAN);
		setCancelHitting(true);
		startNewRound();
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

	}

	public void startNewRound(){
		checkWin();
		if(getGamePlayers().size() > 1){
			UUID first = getRandom(getGamePlayers());
			getGamePlayers().remove(first);
			isFighting.add(first);
			fighting.put(1,first);

			UUID second = getRandom(getGamePlayers());
			getGamePlayers().remove(second);
			isFighting.add(second);
			fighting.put(2,second);

			ItemStack stick = new ItemBuilder(Material.STICK)
					.setDisplayName(Common.colorMessage("&d&lKnockBack stick"))
					.addEnchant(Enchantment.KNOCKBACK,1)
					.build();

			Player firstPlayer = Bukkit.getPlayer(first);
			if(firstPlayer != null){
				firstPlayer.teleport(getFirstSpawn());
				firstPlayer.getInventory().addItem(stick);
				addCountDownState(firstPlayer);
			}
			Player secondPlayer = Bukkit.getPlayer(second);
			if(secondPlayer != null){
				secondPlayer.teleport(getSecondSpawn());
				secondPlayer.getInventory().addItem(stick);
				addCountDownState(secondPlayer);
			}
			startCountDown(10);
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

					if (getSpectators().contains(damager)) {
						e.setCancelled(true);
					}

					if (isFighting.contains(player.getUniqueId()) && !getSpectators().contains(damager)) {
						e.setCancelled(false);
						e.setDamage(0.0D);
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerMoveToDeath(PlayerMoveEvent event) {

		if (getCurrentStatus() == Status.BEGAN) {

			Player player = event.getPlayer();


			World world = player.getLocation().getWorld();
			Location playerLoc = player.getLocation();
			Block block = event.getFrom().getBlock();

			if (!(block.getType().name().contains("WATER") || block.getType().name().contains("STATIONARY_WATER"))) {
				return;
			}

			if(!isFighting.contains(player.getUniqueId())) return;

			for(UUID uuid : isFighting){
				Player fighting = Bukkit.getPlayer(uuid);
				if(fighting != null){
					Common.sendMessage(fighting,player.getName() + " got killed!");
				}
			}
			sendGameMessage(player.getName() + " got killed!");


			if (isSpectatorEnabled()) {
				setSpectator(player);
			} else {
				player.teleport(getLobbySpawn());
			}

			player.setFallDistance(0);
			player.getInventory().clear();

			if(fighting.containsKey(1) && fighting.containsKey(2)){
				UUID first = fighting.get(1);
				UUID second = fighting.get(2);

				if(first.equals(player.getUniqueId()) || first == player.getUniqueId()){
					getGamePlayers().remove(player.getUniqueId());
				}else{
					Player firstPlayer = Bukkit.getPlayer(first);
					if(firstPlayer != null){
						addGamePlayer(firstPlayer);
						firstPlayer.teleport(getSpectatorSpawn());
						firstPlayer.getInventory().clear();
					}
				}
				if(second.equals(player.getUniqueId()) || second == player.getUniqueId()){
					getGamePlayers().remove(player.getUniqueId());
				}else{
					Player secondPlayer = Bukkit.getPlayer(second);
					if(secondPlayer != null){
						secondPlayer.getInventory().clear();
						addGamePlayer(secondPlayer);
						secondPlayer.teleport(getSpectatorSpawn());
					}
				}

			}
			fighting.clear();
			isFighting.clear();
			startNewRound();

		}
	}


	public  <T> T getRandom(List<T> list) {
		Random random = new Random();
		return list.isEmpty() ? null : list.get(random.nextInt(list.size()));
	}



	@Override
	public void checkWin() {
		if (getGamePlayers().size() <= 1) {
			setCurrentStatus(Status.OFFLINE);
			UUID playerWon = getGamePlayers().get(0);
			sendVictory(Bukkit.getPlayer(playerWon));
			rollbackPlayer(Bukkit.getPlayer(playerWon));
			getGamePlayers().clear();
			getSpectators().clear();
			Bukkit.getPlayer(playerWon).teleport(getLobbySpawn());
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
		if(isFighting.contains(player.getUniqueId())){
			isFighting.remove(player.getUniqueId());
			player.teleport(getLobbySpawn());
			checkWin();
			player.sendMessage("You have left the " + getName() + " event");
		}

	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e){
		Player player = e.getPlayer();
		if(isPlaying(player)){
			e.setCancelled(true);
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
		player.teleport(getSpectatorSpawn());
	}

	private static Sumo instance;

	public static Sumo getInstance(){
		if(instance == null){
			instance = new Sumo();
		}
		return instance;
	}
}
