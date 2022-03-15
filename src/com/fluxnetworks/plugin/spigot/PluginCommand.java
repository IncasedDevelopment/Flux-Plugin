package com.fluxnetworks.plugin.spigot;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.fluxnetworks.plugin.common.LanguageHandler.Term;
import com.fluxnetworks.plugin.common.Permission;

import net.kyori.adventure.audience.Audience;

public class PluginCommand implements CommandExecutor {

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		final Audience adv = FluxPlugin.getInstance().adventure().sender(sender);

		if (!sender.hasPermission(Permission.COMMAND_FLUX.toString())) {
			adv.sendMessage(FluxPlugin.getInstance().getLanguage().getComponent(Term.COMMAND_NO_PERMISSION));
			return true;
		}

		if (args.length == 1 && (args[0].equalsIgnoreCase("rl") || args[0].equalsIgnoreCase("reload"))) {
			FluxPlugin.getInstance().reload();
			sender.sendMessage("Successfully reloaded all configuration files."); // TODO translate
			return true;
		}

		return false;
	}
}
