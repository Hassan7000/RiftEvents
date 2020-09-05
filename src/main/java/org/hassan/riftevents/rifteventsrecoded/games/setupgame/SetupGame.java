package org.hassan.riftevents.rifteventsrecoded.games.setupgame;

import me.mattstudios.mf.annotations.Permission;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.hassan.riftevents.rifteventsrecoded.RiftEvents;
import org.hassan.riftevents.rifteventsrecoded.utils.Common;
import org.hassan.riftevents.rifteventsrecoded.utils.ConfigHandler;
import org.hassan.riftevents.rifteventsrecoded.utils.SafeNBT;

import java.util.ArrayList;
import java.util.List;

public class SetupGame implements Listener {

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e){
		Player player = e.getPlayer();
		Block block = e.getBlock();
		if(RiftEvents.getInstance().getSetupState().contains(player.getUniqueId())){
			ItemStack item = player.getItemInHand();
			if(item == null || item.getType() == Material.AIR) return;
			List<Location> locations = new ArrayList<>();
			SafeNBT nbt = SafeNBT.get(item);
			if(nbt.hasKey("arenaspawn")){
				String game = nbt.getString("arenaspawn");
				RiftEvents.getInstance().getArenas().set("Games." + game + ".ArenaSpawn", Common.serialize(block.getLocation()));
				ConfigHandler.Configs.ARENAS.saveConfig();
				Common.sendMessage(player,"&aYou have added the arena spawn for " + game);
				RiftEvents.getInstance().getStateBlocks.get(player.getUniqueId()).add(block.getLocation());
			}
			if(nbt.hasKey("lobbyspawn")){
				String game = nbt.getString("lobbySpawn");
				RiftEvents.getInstance().getArenas().set("GameWorld.LobbySpawn",Common.serialize(block.getLocation()));
				ConfigHandler.Configs.ARENAS.saveConfig();
				Common.sendMessage(player,"&aYou have added the lobby spawn");
				RiftEvents.getInstance().getStateBlocks.get(player.getUniqueId()).add(block.getLocation());
			}
			if(nbt.hasKey("spectatorspawn")){
				String game = nbt.getString("spectatorspawn");
				RiftEvents.getInstance().getArenas().set("Games." + game + ".SpectatorSpawn", Common.serialize(block.getLocation()));
				ConfigHandler.Configs.ARENAS.saveConfig();
				Common.sendMessage(player,"&aYou have added the spectatorspawn");
				RiftEvents.getInstance().getStateBlocks.get(player.getUniqueId()).add(block.getLocation());
			}
			if(nbt.hasKey("randomlocation")){
				String game = nbt.getString("randomlocation");
				List<String> spawns = RiftEvents.getInstance().getArenas().getStringList("Games." + game + ".RandomLocations");
				spawns.add(Common.serialize(block.getLocation()));
				RiftEvents.getInstance().getArenas().set("Games." + game + ".RandomLocations", spawns);
				ConfigHandler.Configs.ARENAS.saveConfig();
				Common.sendMessage(player,"&aYou added a Spawn location");
				RiftEvents.getInstance().getStateBlocks.get(player.getUniqueId()).add(block.getLocation());
			}
			if(nbt.hasKey("firstspawn")){
				String game = nbt.getString("firstspawn");
				RiftEvents.getInstance().getArenas().set("Games." + game + ".FirstSpawn", Common.serialize(block.getLocation()));
				ConfigHandler.Configs.ARENAS.saveConfig();
				RiftEvents.getInstance().getStateBlocks.get(player.getUniqueId()).add(block.getLocation());
				Common.sendMessage(player,"&aYou have set the first spawn location for " + game);
			}
			if(nbt.hasKey("secondspawn")){
				String game = nbt.getString("secondspawn");
				RiftEvents.getInstance().getArenas().set("Games." + game + ".SecondSpawn", Common.serialize(block.getLocation()));
				ConfigHandler.Configs.ARENAS.saveConfig();
				RiftEvents.getInstance().getStateBlocks.get(player.getUniqueId()).add(block.getLocation());
				Common.sendMessage(player,"&aYou have set the second spawn location for " + game);
			}
			if(nbt.hasKey("setFirstPos")){
				String game = nbt.getString("setFirstPos");
				RiftEvents.getInstance().getArenas().set("Games." + game + ".FirstPos", Common.serialize(block.getLocation()));
				ConfigHandler.Configs.ARENAS.saveConfig();
				RiftEvents.getInstance().getStateBlocks.get(player.getUniqueId()).add(block.getLocation());
				Common.sendMessage(player,"&aYou have set the first pos location for " + game);
			}
			if(nbt.hasKey("setSecondPos")){
				String game = nbt.getString("setSecondPos");
				RiftEvents.getInstance().getArenas().set("Games." + game + ".SecondPos", Common.serialize(block.getLocation()));
				ConfigHandler.Configs.ARENAS.saveConfig();
				RiftEvents.getInstance().getStateBlocks.get(player.getUniqueId()).add(block.getLocation());
				Common.sendMessage(player,"&aYou have set the second pos location for " + game);
			}

		}
	}

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent e){
		Player player = e.getPlayer();
		if(RiftEvents.getInstance().getSetupState().contains(player.getUniqueId())){
			e.setCancelled(true);
		}
	}
}
