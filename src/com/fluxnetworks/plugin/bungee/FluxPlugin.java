package com.fluxnetworks.plugin.bungee;

import com.fluxnetworks.plugin.common.ApiProvider;
import com.fluxnetworks.plugin.common.CommonObjectsProvider;
import com.fluxnetworks.plugin.common.ExceptionLogger;
import com.fluxnetworks.plugin.common.LanguageHandler;
import com.fluxnetworks.plugin.common.command.AbstractScheduler;
import com.fluxnetworks.plugin.spigot.YamlFileImpl;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class FluxPlugin extends Plugin implements CommonObjectsProvider {

	private static FluxPlugin instance;
	public static FluxPlugin getInstance() { return instance; }

	private Configuration config;
	public Configuration getConfig() { return this.config; }

	private BungeeAudiences adventure;
	@Override public AudienceProvider adventure() { return this.adventure; }

	private LanguageHandler language;
	@Override public LanguageHandler getLanguage() { return this.language; }

	private ApiProvider apiProvider;
	@Override public ApiProvider getApiProvider() { return this.apiProvider; }

	@Override public ExceptionLogger getExceptionLogger() { return new ExceptionLogger(this.getLogger(), false); } // TODO configurable

	private ScheduledTask dataSenderTask;

	@Override
	public void onEnable() {
		instance = this;

		this.adventure = BungeeAudiences.create(this);
		this.language = new LanguageHandler(getDataFolder().toPath().resolve("languages"));

		try {
			reload();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public AbstractScheduler getScheduler() {
		return new AbstractScheduler() {

			@Override
			public void runAsync(final Runnable runnable) {
				runnable.run();
			}

			@Override
			public void runSync(final Runnable runnable) {
				runnable.run();
			}

		};
	}

	public void reload() throws IOException {
		final Path dataFolder = getDataFolder().toPath();
		final Path configFile = dataFolder.resolve("config.yml");

		if (!Files.isRegularFile(configFile)) {
			try (InputStream in = getResourceAsStream("config.yml")) {
				Files.copy(in, configFile);
			}
		}

		this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile.toFile());

		this.apiProvider = new ApiProvider(
				this.getLogger(),
				this.getExceptionLogger(),
				getConfig().getString("api.url"),
				getConfig().getString("api.key"),
				getConfig().getBoolean("api.debug", false),
				getConfig().getBoolean("api.usernames", false),
				getConfig().getInt("api.timeout", 5000),
				getConfig().getBoolean("api.bypass-version-check"));

		try {
			this.getLanguage().updateFiles();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		if (!this.getLanguage().setActiveLanguage(
				this.config.getString("language", LanguageHandler.DEFAULT_LANGUAGE), YamlFileImpl::new)) {
			this.getLogger().severe("LANGUAGE FILE FAILED TO LOAD");
			this.getLogger().severe("THIS IS BAD NEWS, THE PLUGIN WILL BREAK");
			this.getLogger().severe("FIX IMMEDIATELY");
			this.getLogger().severe("In config.yml, set 'language' to '" + LanguageHandler.DEFAULT_LANGUAGE
					+ "' or any other supported language.");
			throw new RuntimeException("Failed to load language file");
		}

		this.dataSenderTask.cancel();

		final int rate = this.getConfig().getInt("server-data-upload-rate", 10);
		final int serverId = getConfig().getInt("server-id");
		if (rate < 0 || serverId < 0) {
			this.dataSenderTask = null;
		} else {
			this.dataSenderTask = getProxy().getScheduler().schedule(this, new ServerDataSender(), rate, rate, TimeUnit.SECONDS);
		}
	}

}
