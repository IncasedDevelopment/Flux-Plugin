package com.fluxnetworks.plugin.spigot.hooks;

import org.bukkit.entity.Player;

public interface PapiParser {

	String parse(Player player, String text);

}
