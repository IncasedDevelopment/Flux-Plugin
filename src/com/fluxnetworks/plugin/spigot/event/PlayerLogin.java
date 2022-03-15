package com.fluxnetworks.plugin.spigot.event;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.fluxnetworks.java_api.FluxAPI;
import com.fluxnetworks.java_api.FluxException;
import com.fluxnetworks.java_api.FluxUser;
import com.fluxnetworks.plugin.common.LanguageHandler.Term;
import com.fluxnetworks.plugin.spigot.Config;
import com.fluxnetworks.plugin.spigot.FluxPlugin;

import net.kyori.adventure.text.Component;

public class PlayerLogin implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();

		FluxPlugin.LOGIN_TIME.put(player.getUniqueId(), System.currentTimeMillis());

		final FileConfiguration config = FluxPlugin.getInstance().getConfig();

		if (config.getBoolean("not-registered-join-message")) {
			Bukkit.getScheduler().runTaskAsynchronously(FluxPlugin.getInstance(), () -> {
				final Optional<FluxAPI> optApi = FluxPlugin.getInstance().getFluxApi();
				if (optApi.isPresent()) {
					try {
						final Optional<FluxUser> user = optApi.get().getUser(player.getUniqueId());
						if (!user.isPresent()) {
							Bukkit.getScheduler().runTask(FluxPlugin.getInstance(), () -> {
								final Component message = FluxPlugin.getInstance().getLanguage().getComponent(Term.JOIN_NOTREGISTERED);
								FluxPlugin.getInstance().adventure().player(event.getPlayer()).sendMessage(message);
							});
						}
					} catch (final FluxException e) {
						FluxPlugin.getInstance().getExceptionLogger().logException(e);
					}
				} else {
					FluxPlugin.getInstance().getLogger().warning("Not sending join message, API is not working properly.");
				}
			});
		}

		/*if (!config.getBoolean("join-notifications")) {
			return;
		}

		Bukkit.getScheduler().runTaskAsynchronously(FluxPlugin.getInstance(), () -> {
			FluxPlayer FluxPlayer = new FluxPlayer(player.getUniqueId(), FluxPlugin.baseApiURL);
			if (!FluxPlayer.exists()) {
				return;
			}

			if (!FluxPlayer.isValidated()) {
				player.sendMessage(Message.ACCOUNT_NOT_VALIDATED.getMessage());
				return;
			}

			int messages;
			int alerts;*/

			//player.sendMessage("notifications are temporarely disabled");
			/*try {
				messages = FluxPlayer.getNotifications();
				alerts = FluxPlayer.getAlertCount();
			} catch (FluxException e) {
				String errorMessage = Message.NOTIFICATIONS_ERROR.getMessage().replace("%error%", e.getMessage());
				player.sendMessage(errorMessage);
				e.printStackTrace();
				return;
			}


			String pmMessage = Message.NOTIFICATIONS_MESSAGES.getMessage().replace("%pms%", messages + "");
			String alertMessage = Message.NOTIFICATIONS_ALERTS.getMessage().replace("%alerts%", alerts + "");
			String noNotifications = Message.NO_NOTIFICATIONS.getMessage();

			if (alerts == 0 && messages == 0) {
				player.sendMessage(noNotifications);
			} else if (alerts == 0) {
				player.sendMessage(pmMessage);
			} else if (messages == 0) {
				player.sendMessage(alertMessage);
			} else {
				player.sendMessage(alertMessage);
				player.sendMessage(pmMessage);
			}

		});*/
	}

}
