package org.hassan.riftevents.rifteventsrecoded.utils;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Common {

	public static void sendMessage(Player player, String mesecondsage) {

		player.sendMessage(ChatColor.translateAlternateColorCodes('&', mesecondsage));
	}

	public static void sendMessage(CommandSender sender, String mesecondsage) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', mesecondsage));
	}

	public static void broadcastMessage(String message){
		Bukkit.broadcastMessage(colorMessage(message));
	}

	public static void executeConsoleCommand(String command) {
		Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
	}

	public static void executePlayerCommand(Player player, String command) {
		player.performCommand(command);
	}

	public static String colorMessage(String mesecondsage) {
		return ChatColor.translateAlternateColorCodes('&', mesecondsage);
	}

	public static Location deserializeL(String s) {
		String[] st = s.split(";");
		Location loc = new Location(Bukkit.getWorld(st[0]), Double.parseDouble(st[1]), Double.parseDouble(st[2]),
				Double.parseDouble(st[3]));
		loc.setPitch(Float.parseFloat(st[4]));
		loc.setYaw(Float.parseFloat(st[5]));
		return loc;
	}

	public static String serialize(Location l) {
		return l.getWorld().getName() + ";" + l.getX() + ";"
				+ l.getY() + ";" + l.getZ() + ";" + l.getPitch() + ";"
				+ l.getYaw();
	}

	public static String formatValue(float value) {
		String arr[] = {"", "K", "M", "B", "T", "P", "E"};
		int index = 0;
		while ((value / 1000) >= 1) {
			value = value / 1000;
			index++;
		}
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		return String.format("%s%s", decimalFormat.format(value), arr[index]);
	}

	public static String checkTime(int seconds) {
		int h = seconds/ 3600;
		int m = (seconds % 3600) / 60;
		int s = seconds % 60;
		String sh = (h > 0 ? String.valueOf(h) + "h" : "");
		String sm = (m < 10 && m > 0 && h > 0 ? "0" : "") + (m > 0 ? (h > 0 && s == 0 ? String.valueOf(m) : String.valueOf(m) + "m") : "");
		String ss = (s == 0 && (h > 0 || m > 0) ? "" : (s < 10 && (h > 0 || m > 0) ? "0" : "") + String.valueOf(s) + "s");
		return sh + (h > 0 ? " " : "") + sm + (m > 0 ? " " : "") + ss;
	}



}
