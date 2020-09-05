package org.hassan.riftevents.rifteventsrecoded.commands;

import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hassan.riftevents.rifteventsrecoded.RiftEvents;
import org.hassan.riftevents.rifteventsrecoded.games.Dropper;
import org.hassan.riftevents.rifteventsrecoded.games.Spleef;
import org.hassan.riftevents.rifteventsrecoded.games.Sumo;
import org.hassan.riftevents.rifteventsrecoded.utils.Common;

import java.util.ArrayList;

@Command("events")
@Alias("riftevents")
public class EventCommand extends CommandBase {

	@Default
	public void defaultCommand(final CommandSender sender) {
		for(String message : RiftEvents.getInstance().getConfig().getStringList("Help-Message")){
			Common.sendMessage(sender,message);
		}
	}

	@SubCommand("join")
	public void subCommand(final CommandSender sender, String event) {
		Player player = (Player) sender;
		if(event == null){
			sender.sendMessage("Please make sure to type a event");
			return;
		}
		if(event.equalsIgnoreCase("Dropper")){
			Dropper.getInstance().join(player, false);
		}
		if(event.equalsIgnoreCase("Sumo")){
			Sumo.getInstance().join(player,false);
		}
		if(event.equalsIgnoreCase("Spleef")){
			Spleef.getInstance().join(player,false);
		}
	}

	@SubCommand("paste")
	@Permission("riftevents.admin")
	public void pasteCommand(final CommandSender sender){
		Spleef.getInstance().pasteMap();
	}


	@SubCommand("start")
	@Permission("riftevents.admin")
	public void startCommand(final CommandSender sender, String event){
		Player player = (Player) sender;
		if(event == null){
			sender.sendMessage("Please make sure to type a event");
			return;
		}
		if(event.equalsIgnoreCase("Dropper")){
			Dropper.getInstance().start();
		}
		if(event.equalsIgnoreCase("Sumo")){
			Sumo.getInstance().start();
		}
		if(event.equalsIgnoreCase("Spleef")){
			Spleef.getInstance().start();
		}
	}
	@SubCommand("stop")
	@Permission("riftevents.admin")
	public void stopCommand(final CommandSender sender, String event){
		if(event == null){
			sender.sendMessage("Please make sure to type a event");
			return;
		}
		if(event.equalsIgnoreCase("Dropper")){
			Dropper.getInstance().stop(false);
		}else if(event.equalsIgnoreCase("Sumo")){
			Sumo.getInstance().stop(false);
		}else if(event.equalsIgnoreCase("Spleef")){
			Spleef.getInstance().stop(false);
		}

	}
	@SubCommand("setup")
	@Permission("riftevents.admin")
	public void setupCommand(final CommandSender sender, String event){
		Player player = (Player) sender;
		if(event == null){
			sender.sendMessage("Please make sure to type a event");
			return;
		}
		if(RiftEvents.getInstance().getSetupState().contains(player.getUniqueId())){
			RiftEvents.getInstance().getSetupState().remove(player.getUniqueId());
			player.getInventory().clear();
			Common.sendMessage(player,"&aSetup state is disabled");
			for(Location location : RiftEvents.getInstance().getStateBlocks.get(player.getUniqueId())){
				location.getBlock().setType(Material.AIR);
			}
		}else{
			if(event.equalsIgnoreCase("Dropper")){
				Dropper.getInstance().setupGame(player,event);
				Common.sendMessage(player,"&cSetup state is enabled");
				RiftEvents.getInstance().getStateBlocks.put(player.getUniqueId(),new ArrayList<>());
			}
			if(event.equalsIgnoreCase("Sumo")){
				Sumo.getInstance().setupGame(player,event);
				Common.sendMessage(player,"&cSetup state is enabled");
				RiftEvents.getInstance().getStateBlocks.put(player.getUniqueId(),new ArrayList<>());
			}
			if(event.equalsIgnoreCase("Spleef")){
				Spleef.getInstance().setupGame(player,event);
				Common.sendMessage(player,"&cSetup state is enabled");
				RiftEvents.getInstance().getStateBlocks.put(player.getUniqueId(),new ArrayList<>());
			}
		}

	}

}
