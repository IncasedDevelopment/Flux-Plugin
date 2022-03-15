package com.fluxnetworks.plugin.spigot;

import org.bukkit.ChatColor;

public class Chat {

	public static String convertColors(final String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}

}