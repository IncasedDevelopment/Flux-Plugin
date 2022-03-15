package com.fluxnetworks.plugin.bungee;

import com.google.gson.JsonObject;
import com.fluxnetworks.java_api.ApiError;
import com.fluxnetworks.java_api.FluxException;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ServerDataSender implements Runnable {

	@Override
	public void run() {
		final int serverId = FluxPlugin.getInstance().getConfig().getInt("server-id");

		final JsonObject data = new JsonObject();
		data.addProperty("tps", 20); // TODO Send real TPS
		data.addProperty("time", System.currentTimeMillis());
		data.addProperty("free-memory", Runtime.getRuntime().freeMemory());
		data.addProperty("max-memory", Runtime.getRuntime().maxMemory());
		data.addProperty("allocated-memory", Runtime.getRuntime().totalMemory());
		data.addProperty("server-id", serverId);

		final JsonObject players = new JsonObject();

		for (final ProxiedPlayer player : FluxPlugin.getInstance().getProxy().getPlayers()) {
			final JsonObject playerInfo = new JsonObject();

			playerInfo.addProperty("name", player.getName());
			playerInfo.addProperty("address", player.getSocketAddress().toString());

			players.add(player.getUniqueId().toString().replace("-", ""), playerInfo);
		}

		data.add("players", players);

		FluxPlugin.getInstance().getProxy().getScheduler().runAsync(FluxPlugin.getInstance(), () ->
			FluxPlugin.getInstance().getApiProvider().getFluxApi().ifPresent((api) -> {
				try {
					api.submitServerInfo(data);
				} catch (final ApiError e) {
					if (e.getError() == ApiError.INVALID_SERVER_ID) {
						FluxPlugin.getInstance().getLogger().warning("Server ID is incorrect. Please enter a correct server ID or disable the server data uploader.");
					} else {
						FluxPlugin.getInstance().getExceptionLogger().logException(e);
					}
				} catch (final FluxException e) {
					FluxPlugin.getInstance().getExceptionLogger().logException(e);
				}
			})
		);
	}

}
