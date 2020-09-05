package org.hassan.riftevents.rifteventsrecoded.games;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.hassan.riftevents.rifteventsrecoded.RiftEvents;
import org.hassan.riftevents.rifteventsrecoded.games.gameobject.Game;
import org.hassan.riftevents.rifteventsrecoded.utils.Common;
import org.hassan.riftevents.rifteventsrecoded.utils.ItemBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Spleef extends Game {


	public Spleef() {
		super("Spleef");
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

		ItemStack shovel = new ItemBuilder(Material.GOLD_SPADE)
				.setDisplayName(Common.colorMessage("&d&lSpleef Shovel"))
				.addEnchant(Enchantment.DIG_SPEED,2)
				.build();
		addItemToGamePlayers(shovel);
		sendGameMessage("GAME HAS STARTED");

	}


	@EventHandler
	public void onLavaHit(PlayerMoveEvent event){
		if (getCurrentStatus() == Status.BEGAN) {
			Player player = event.getPlayer();
			Block block = event.getFrom().getBlock();
			if (!(block.getType().name().contains("LAVA") || block.getType().name().contains("STATIONARY_LAVA"))) {
				return;
			}

			if(getGamePlayers().contains(player.getUniqueId())){
				if (isSpectatorEnabled() && getGamePlayers().size() > 1) {
					setSpectator(player);
				} else {
					Bukkit.getScheduler().runTaskLater(RiftEvents.getInstance(), () -> rollbackPlayer(player), 1L);
				}

				removeGamePlayer(player);
				checkWin();
			}

		}


		}

	public void findLocations() {
		Location start = getFirstPos();
		Location end = getSecondPos();
		List<Block> blocks = new ArrayList<>();
		int topBlockX = (start.getBlockX() < end.getBlockX() ? end.getBlockX() : start.getBlockX());
		int bottomBlockX = (start.getBlockX() > end.getBlockX() ? end.getBlockX() : start.getBlockX());

		int topBlockY = (start.getBlockY() < end.getBlockY() ? end.getBlockY() : start.getBlockY());
		int bottomBlockY = (start.getBlockY() > end.getBlockY() ? end.getBlockY() : start.getBlockY());

		int topBlockZ = (start.getBlockZ() < end.getBlockZ() ? end.getBlockZ() : start.getBlockZ());
		int bottomBlockZ = (start.getBlockZ() > end.getBlockZ() ? end.getBlockZ() : start.getBlockZ());

		for(int x = bottomBlockX; x <= topBlockX; x++)
		{
			for(int z = bottomBlockZ; z <= topBlockZ; z++)
			{
				for(int y = bottomBlockY; y <= topBlockY; y++)
				{
					Block block = start.getWorld().getBlockAt(x, y, z);

					if(block.getType() == Material.SNOW_BLOCK){
						RiftEvents.getInstance().fastBlockUpdate.addBlock(block.getLocation(),block.getType(),block.getData());
					}

				}
			}
		}
	}

	public void pasteMap(){
		RiftEvents.getInstance().fastBlockUpdate.run();
	}

	@Override
	public void stop(boolean message) {
		for(UUID uuid : getGamePlayers()){
			Player player = Bukkit.getPlayer(uuid);
			if(player == null) continue;
			player.sendMessage("Dropper game mode has been stopped");
		}

		for(UUID uuid : getSpectators()){
			Player player = Bukkit.getPlayer(uuid);
			if(player == null) continue;
			player.getInventory().clear();
			player.sendMessage("Dropper game mode has been stopped");
			teleportGamePlayer(player,getLobbySpawn());
		}

		teleportGamePlayers(getLobbySpawn());
		getGamePlayers().clear();
		getSpectators().clear();
		setCurrentStatus(Status.OFFLINE);
		pasteMap();

	}



	@Override
	public void checkWin() {
		if(getGamePlayers().size() <= 1){
			setCurrentStatus(Status.OFFLINE);
			UUID playerWon = getGamePlayers().get(0);
			sendVictory(Bukkit.getPlayer(playerWon));
			rollbackPlayer(Bukkit.getPlayer(playerWon));
			getGamePlayers().clear();
			getSpectators().clear();
			Bukkit.getPlayer(playerWon).teleport(getLobbySpawn());
			getGamePlayers().clear();
			pasteMap();
		}
	}

	@Override
	public void leave(Player player) {
		removeGamePlayer(player);
		getSpectators().remove(player.getUniqueId());
		player.getInventory().clear();

		teleportGamePlayer(player,getLobbySpawn());
		Common.sendMessage(player,"You have left " + getName());
		checkWin();
	}

	@EventHandler
	public void snowBreak(BlockBreakEvent event) {
		if (getCurrentStatus() == Status.BEGAN) {
			Bukkit.broadcastMessage("WORKS");
			Block block = event.getBlock();
			if (block.getType() == Material.SNOW_BLOCK && getGamePlayers().contains(event.getPlayer().getUniqueId())) {
				if(!getGamePlayers().contains(event.getPlayer().getUniqueId())) return;

				block.setType(Material.AIR);
				event.getPlayer().getInventory().addItem(new ItemStack(Material.SNOW_BALL, 4));
			}else{
				event.setCancelled(true);
			}
		}else if(getCurrentStatus() == Status.STARTING && getGamePlayers().contains(event.getPlayer().getUniqueId())){
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void snowballHitSnow(ProjectileHitEvent event) { // Needs changing
		if (getCurrentStatus() == Status.BEGAN) {
			ProjectileSource shooter = event.getEntity().getShooter();
			if (!(shooter instanceof Player))
				return;

			Entity entity = event.getEntity();

			if (entity instanceof Snowball) {
				Location loc = entity.getLocation();
				Vector vec = entity.getVelocity();
				Location loc2 = new Location(loc.getWorld(), loc.getX() + vec.getX(), loc.getY() + vec.getY(), loc.getZ() + vec.getZ());
				if (loc2.getBlock().getType() == Material.SNOW_BLOCK) {
					loc2.getBlock().setType(Material.AIR);
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
		teleportGamePlayer(player,getArenaSpawn());
	}

	private static Spleef instance;

	public static Spleef getInstance(){
		if(instance == null){
			instance = new Spleef();
		}
		return instance;
	}
}
