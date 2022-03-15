package com.fluxnetworks.plugin.spigot;

import com.fluxnetworks.java_api.*;
import com.fluxnetworks.plugin.common.LanguageHandler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class UserSyncTask implements Runnable {

	@Override
	public void run() {
		Logger logger = FluxPlugin.getInstance().getLogger();
		FileConfiguration config = FluxPlugin.getInstance().getConfig();
		boolean doLog = config.getBoolean("user-sync.log", true);
		Runnable runAfter = null;
		if (config.getBoolean("user-sync.whitelist.enabled", false)) {
			runAfter = () -> this.syncWhitelist(doLog, logger);
		}

		if (config.getBoolean("user-sync.bans.enabled", false)) {
			syncBans(runAfter, doLog, logger);
		} else if (runAfter != null) {
			runAfter.run();
		}
	}

	@Nullable
	private Set<UUID> getUuids(boolean doLog, Consumer<FilteredUserListBuilder> builderConfigurator) {
		List<FluxUser> users;
		try {
			final Optional<FluxAPI> optApi = FluxPlugin.getInstance().getFluxApi();
			if (optApi.isPresent()) {
				FilteredUserListBuilder builder = optApi.get().getRegisteredUsers();
				builderConfigurator.accept(builder);
				users = builder.makeRequest();
			} else {
				FluxPlugin.getInstance().getLogger().warning("Skipped sync, it looks like the API is not working properly.");
				return null;
			}
		} catch (final FluxException e) {
			FluxPlugin.getInstance().getLogger().warning(
					"An error occured while getting a list of registered users from the website for the bans sync feature.");
			FluxPlugin.getInstance().getExceptionLogger().logException(e);
			return null;
		}

		final Set<UUID> uuids = new HashSet<>();
		final Set<String> excludes = new HashSet<>(FluxPlugin.getInstance().getConfig().getStringList("user-sync.exclude"));
		for (final FluxUser user : users) {
			try {
				if (FluxPlugin.getInstance().getApiProvider().useUsernames()) {
					String name = user.getUsername();
					if (!excludes.contains(name)) {
						UUID offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
						uuids.add(offlineUuid);
					} else if (doLog) {
						FluxPlugin.getInstance().getLogger().info("Ignoring user " + name);
					}
				} else {
					final Optional<UUID> optUuid = user.getUniqueId();
					if (optUuid.isPresent()) {
						UUID uuid = optUuid.get();
						if (!excludes.contains(uuid.toString())) {
							uuids.add(optUuid.get());
						} else if (doLog) {
							FluxPlugin.getInstance().getLogger().info("Ignoring user " + optUuid.get());
						}
					} else {
						FluxPlugin.getInstance().getLogger().warning("Website user " + user.getUsername() + " does not have a UUID!");
					}
				}
			} catch (final FluxException e) {
				throw new IllegalStateException("Getting a user uuid should never fail with a network error, it is cached from the listUsers response", e);
			}
		}
		return uuids;
	}

	private void syncBans(@Nullable Runnable onComplete, boolean doLog, @NotNull Logger logger) {
		if (doLog) {
			logger.info("Starting bans sync, retrieving list of banned users...");
		}
		Bukkit.getScheduler().runTaskAsynchronously((FluxPlugin.getInstance()), () -> {
			Set<UUID> bannedUuids = getUuids(doLog, b -> b.withFilter(UserFilter.BANNED, true));
			if (bannedUuids == null) {
				return;
			}
			Bukkit.getScheduler().runTask(FluxPlugin.getInstance(), () -> {
				Set<OfflinePlayer> banned = Bukkit.getBannedPlayers();
				for (UUID bannedUuid : bannedUuids) {
					OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(bannedUuid);
					if (!banned.contains(bannedPlayer)) {
						banned.add(bannedPlayer);
						if (doLog) {
							logger.info("Added " + bannedUuid + " to the ban list");
						}
						if (bannedPlayer.isOnline()) {
							String message = FluxPlugin.getInstance().getLanguage()
									.getLegacyMessage(LanguageHandler.Term.USER_SYNC_KICK);
							((Player) bannedPlayer).kickPlayer(message);
						}
					}
				}
				if (doLog) {
					logger.info("Retrieving list of unbanned players...");
				}
				Bukkit.getScheduler().runTaskAsynchronously(FluxPlugin.getInstance(), () -> {
					Set<UUID> unbannedUuids = getUuids(doLog, b -> b.withFilter(UserFilter.BANNED, false));
					if (unbannedUuids == null) {
						return;
					}
					Bukkit.getScheduler().runTask(FluxPlugin.getInstance(), () -> {
						Set<OfflinePlayer> banned2 = Bukkit.getBannedPlayers();
						for (UUID unbannedUuid : unbannedUuids) {
							OfflinePlayer unbannedPlayer = Bukkit.getOfflinePlayer(unbannedUuid);
							if (banned2.contains(unbannedPlayer)) {
								banned2.remove(unbannedPlayer);
								if (doLog) {
									logger.info("Removed " + unbannedUuid + " from the ban list");
								}
							}
						}
						if (onComplete != null) {
							onComplete.run();
						}
					});
				});
			});
		});
	}

	private void syncWhitelist(boolean doLog, @NotNull Logger logger) {
		final boolean verifiedOnly = FluxPlugin.getInstance().getConfig().getBoolean("user-sync.whitelist.verified-only");
		final boolean discordLinkedOnly = FluxPlugin.getInstance().getConfig().getBoolean("user-sync.whitelist.discord-linked-only");

		if (doLog) {
			logger.info("Starting auto-whitelist, retrieving list of registered users...");
		}

		Bukkit.getScheduler().runTaskAsynchronously(FluxPlugin.getInstance(), () -> {
			Set<UUID> websiteUuids = getUuids(doLog, b -> {
				b.withFilter(UserFilter.BANNED, false);
				if (verifiedOnly) {
					b.withFilter(UserFilter.VERIFIED, true);
				}
				if (discordLinkedOnly) {
					b.withFilter(UserFilter.DISCORD_LINKED, true);
				}
			});

			if (websiteUuids == null) {
				return;
			}

			Bukkit.getScheduler().runTask(FluxPlugin.getInstance(), () -> {
				if (doLog) {
					logger.info("Done, updating bukkit whitelist...");
				}

				// Whitelist players who are not whitelisted but should be
				for (final UUID websiteUuid : websiteUuids) {
					final OfflinePlayer player = Bukkit.getOfflinePlayer(websiteUuid);
					if (!player.isWhitelisted()) {
						player.setWhitelisted(true);
						if (doLog) {
							logger.info("Added " + (player.getName() == null ? websiteUuid.toString() : player.getName()) + " to the whitelist.");
						}
					}
				}

				if (doLog) {
					logger.info("Done, now retrieving a list of banned users so we can remove them from the whitelist...");
				}

				Bukkit.getScheduler().runTaskAsynchronously(FluxPlugin.getInstance(), () -> {
					Set<UUID> bannedUuids = getUuids(doLog, b -> {
						b.any();
						b.withFilter(UserFilter.BANNED, true);
						if (verifiedOnly) {
							b.withFilter(UserFilter.VERIFIED, false);
						}
						if (discordLinkedOnly) {
							b.withFilter(UserFilter.DISCORD_LINKED, false);
						}
					});
					if (bannedUuids == null) {
						return;
					}
					Bukkit.getScheduler().runTask(FluxPlugin.getInstance(), () -> {
						for (UUID bannedUuid : bannedUuids) {
							OfflinePlayer player = Bukkit.getOfflinePlayer(bannedUuid);
							if (player.isWhitelisted()) {
								player.setWhitelisted(false);
								if (doLog) {
									logger.info("Removed " + (player.getName() == null ? bannedUuid.toString() : player.getName()) + " from the whitelist");
								}
								if (player.isOnline()) {
									String message = FluxPlugin.getInstance().getLanguage()
											.getLegacyMessage(LanguageHandler.Term.USER_SYNC_KICK);
									((Player) player).kickPlayer(message);
								}
							}
						}
					});
				});
			});
		});
	}

}
