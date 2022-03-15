package com.fluxnetworks.plugin.spigot;

import com.fluxnetworks.java_api.Announcement;
import com.fluxnetworks.java_api.FluxException;
import com.fluxnetworks.java_api.FluxUser;
import com.fluxnetworks.plugin.common.ApiProvider;
import com.fluxnetworks.plugin.common.LanguageHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xyz.derkades.derkutils.ListUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class AnnouncementTask implements Runnable {

	@Override
	public void run() {
		ApiProvider apiProvider = FluxPlugin.getInstance().getApiProvider();
		apiProvider.getFluxApi().ifPresent(api -> {
			@Nullable String filterDisplay = FluxPlugin.getInstance().getConfig().getString("announcements.display");
			int delay = 0;
			for (Player player : Bukkit.getOnlinePlayers()) {
				UUID uuid = player.getUniqueId();
				String name = player.getName();
				// add delay so requests are spread out a bit
				Bukkit.getScheduler().runTaskLaterAsynchronously(FluxPlugin.getInstance(), () -> {
					List<Announcement> announcements;
					try {
						Optional<FluxUser> optUser = apiProvider.userFromPlayer(api, uuid, name);
						if (optUser.isPresent()) {
							announcements = api.getAnnouncements(optUser.get());
						} else {
							// TODO uncomment when the java api has this method again
//							announcements = api.getAnnouncements();
							throw new UnsupportedOperationException("Getting announcements for guests is temporarily unsupported");
						}
					} catch (FluxException e) {
						FluxPlugin.getInstance().getExceptionLogger().logException(e);
						return;
					}
					if (filterDisplay != null) {
						announcements = announcements.stream().filter(a -> a.getDisplayPages().contains(filterDisplay)).collect(Collectors.toList());
					}
					if (!announcements.isEmpty()) {
						Announcement announcement = ListUtils.choice(announcements);
						String announcementMessage = announcement.getMessage();
						Bukkit.getScheduler().runTask(FluxPlugin.getInstance(), () -> {
							Player player2 = Bukkit.getPlayer(uuid);
							if (player2 == null) {
								// Player left
								return;
							}
							Component message = FluxPlugin.getInstance().getLanguage()
									.getComponent(LanguageHandler.Term.WEBSITE_ANNOUNCEMENT, "message", announcementMessage);
							FluxPlugin.getInstance().adventure().player(player2).sendMessage(message);
						});
					}
				}, delay);
				delay += 5; // roughly 4 requests per second
			}
		});
	}

}
