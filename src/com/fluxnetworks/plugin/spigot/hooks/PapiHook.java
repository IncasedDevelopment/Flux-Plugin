package com.fluxnetworks.plugin.spigot.hooks;

import org.bukkit.entity.Player;

import com.fluxnetworks.plugin.spigot.FluxPlugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;

public class PapiHook extends PlaceholderExpansion {

	@Override
	public String onPlaceholderRequest(final Player player, final String identifier) {
		if (identifier.equals("notifications") && player != null) {
			PlaceholderCacher cacher = FluxPlugin.getInstance().getPlaceholderCacher();
			if (cacher != null) {
				int count = cacher.getNotificationCount(player);
				return count > 0 ? String.valueOf(count) : "?";
			} else {
				return "<placeholders disabled>";
			}
		}
		return null;
	}

	@Override
	public @NotNull String getAuthor() {
		return String.join(", ", FluxPlugin.getInstance().getDescription().getAuthors());
	}

	@Override
	public @NotNull String getIdentifier() {
		return "flux";
	}

	@Override
	public @NotNull String getVersion() {
		return FluxPlugin.getInstance().getDescription().getVersion();
	}

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

}