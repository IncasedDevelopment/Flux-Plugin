package com.fluxnetworks.plugin.common;

import com.fluxnetworks.java_api.*;
import com.fluxnetworks.java_api.exception.UnknownFluxVersionException;
import com.fluxnetworks.java_api.logger.ApiLogger;
import com.fluxnetworks.java_api.logger.JavaLoggerLogger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("OptionalAssignedToNull")
public class ApiProvider {

	private static final String USER_AGENT = "Flux-Plugin";

	private Optional<FluxAPI> cachedApi; // null if not cached

	private final @NotNull Logger logger;
	private final @NotNull ExceptionLogger exceptionLogger;
	private final @Nullable String apiUrl;
	private final @Nullable String apiKey;
	private final boolean debug;
	private final boolean usernames;
	private final int timeout;
	private final boolean bypassVersionCheck;

	public ApiProvider(final @NotNull Logger logger,
					   final @NotNull ExceptionLogger exceptionLogger,
					   final @Nullable String apiUrl,
					   final @Nullable String apiKey,
					   final boolean debug,
					   final boolean usernames,
					   final int timeout,
					   final boolean bypassVersionCheck) {
		this.logger = logger;
		this.exceptionLogger = exceptionLogger;
		this.apiUrl = apiUrl;
		this.apiKey = apiKey;
		this.debug = debug;
		this.usernames = usernames;
		this.timeout = timeout;
		this.bypassVersionCheck = bypassVersionCheck;
	}

	public synchronized Optional<FluxAPI> getFluxApi() {
		Objects.requireNonNull(exceptionLogger, "Exception logger not initialized before API was requested. This is a bug.");

		if (this.cachedApi != null) {
			return this.cachedApi;
		}

		final ApiLogger debugLogger = this.debug
				? new JavaLoggerLogger(this.logger, Level.INFO, "[Flux-Java-API] ")
				: null;

		this.cachedApi = Optional.empty();

		try {
			if (this.apiUrl == null || this.apiUrl.isEmpty()) {
				this.logger.severe("You have not entered an API URL in the config. Please get your site's API URL from StaffCP > Configuration > API and reload the plugin.");
			} else if (this.apiKey == null || this.apiKey.isEmpty()) {
				this.logger.severe("You have not entered an API key in the config. Please get your site's API key from StaffCP > Configuration > API and reload the plugin.");
			} else {
				URL url = null;
				try {
					url = new URL(this.apiUrl);
				} catch (MalformedURLException e){
					this.logger.severe("You have entered an invalid API URL or not entered one at all. Please get an up-to-date API URL from StaffCP > Configuration > API and reload the plugin.");
					this.logger.severe("Error message: '" + e.getMessage() + "'");
				}

				if (url != null) {
					final FluxAPI api = FluxAPI.builder(url, this.apiKey)
							.userAgent(USER_AGENT)
							.withCustomDebugLogger(debugLogger)
							.withTimeoutMillis(this.timeout)
							.build();

					final Website info = api.getWebsite();
					try {
						FluxVersion version = info.getParsedVersion();
						if (this.bypassVersionCheck) {
							this.logger.warning("Bypassing version checks, use at your own risk!");
							this.cachedApi = Optional.of(api);
						} else if (GlobalConstants.SUPPORTED_WEBSITE_VERSIONS.contains(version)) {
							this.cachedApi = Optional.of(api);
						} else if (GlobalConstants.DEPRECATED_WEBSITE_VERSIONS.contains(version)) {
							this.logger.warning("Support for your Flux Networks version (" + version + ") is deprecated, some functionality may be broken. Please upgrade to a newer version of Flux Networks as soon as possible.");
							this.cachedApi = Optional.of(api);
						} else {
							this.logger.severe("Your website runs a version of Flux Networks (" + version + ") that is not supported by this version of the plugin. Note that usually only the newest one or two Flux Networks versions are supported.");
						}
					} catch (final UnknownFluxVersionException e) {
						this.logger.severe("The plugin doesn't recognize the Flux Networks version you are using. Ensure you are running a recent version of the plugin and Flux Networks v2.");
					}
				}
			}
		} catch (final ApiError e) {
			if (e.getError() == ApiError.INVALID_API_KEY) {
				this.logger.severe("You have entered an invalid API key. Please get an up-to-date API URL from StaffCP > Configuration > API and reload the plugin.");
			} else {
				this.logger.severe("Encountered an unexpected error code " + e.getError() + " while trying to connect to your website. Enable api debug mode in the config file for more details. When you think you've fixed the problem, reload the plugin to attempt connecting again.");
			}
		} catch (final FluxException e) {
			this.logger.warning("Encountered an error while connecting to the website. This message is expected if your site is down temporarily and can be ignored if the plugin works fine otherwise. If the plugin doesn't work as expected, please enable api-debug-mode in the config and run /nlpl reload to get more information.");
			// Do not cache so it immediately tries again the next time. These types of errors may fix on their
			// own, so we don't want to break the plugin until the administrator reloads.
			if (this.debug) {
				this.logger.warning("Debug is enabled, printing full error message:");
				exceptionLogger.logException(e);
			}

			this.cachedApi = null;
			return Optional.empty();
		}

		return this.cachedApi;
	}

	public boolean useUsernames() {
		return usernames;
	}

	public Optional<FluxUser> userFromPlayer(@NotNull FluxAPI api, @NotNull Player player) throws FluxException {
		return this.useUsernames() ? api.getUser(player.getName()) : api.getUser(player.getUniqueId());
	}

	public Optional<FluxUser> userFromPlayer(@NotNull FluxAPI api, @NotNull UUID uuid, @NotNull String name) throws FluxException {
		return this.useUsernames() ? api.getUser(name) : api.getUser(uuid);
	}

}
