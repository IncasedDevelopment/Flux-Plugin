package com.fluxnetworks.plugin.spigot.event;

import com.fluxnetworks.java_api.FluxAPI;
import com.fluxnetworks.java_api.FluxException;
import com.fluxnetworks.java_api.FluxUser;
import com.fluxnetworks.plugin.spigot.FluxPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class PlayerBan implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBan(PlayerKickEvent event) {
		Player player = event.getPlayer();
		if (!player.isBanned()) {
			return;
		}

		if (!FluxPlugin.getInstance().getConfig().getBoolean("auto-ban-on-website", false)) {
			return;
		}

		final UUID uuid = player.getUniqueId();
		final String name = player.getName();
		final Logger logger = FluxPlugin.getInstance().getLogger();

		Bukkit.getScheduler().runTaskAsynchronously(FluxPlugin.getInstance(), () -> {
			Optional<FluxAPI> optApi = FluxPlugin.getInstance().getFluxApi();
			if (optApi.isPresent()) {
				FluxAPI api = optApi.get();
				try {
					Optional<FluxUser> optUser = FluxPlugin.getInstance().getApiProvider().userFromPlayer(api, uuid, name);
					if (optUser.isPresent()) {
						FluxUser user = optUser.get();
						user.banUser();
						logger.info("Banned " + name + " on the website.");
					} else {
						logger.info(name + " does not have a website account.");
					}
				} catch (FluxException e) {
					logger.warning("An error occured while trying to find " + name + "'s website account: " + e.getMessage());
				}
			} else {
				logger.warning("Skipped trying to ban " + name + " on website, API is not working properly.");
			};
		});
	}

}
