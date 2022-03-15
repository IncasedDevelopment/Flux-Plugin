package com.fluxnetworks.plugin.common.command;

import com.fluxnetworks.java_api.FluxAPI;
import com.fluxnetworks.java_api.FluxException;
import com.fluxnetworks.java_api.FluxUser;
import com.fluxnetworks.java_api.Notification;
import com.fluxnetworks.plugin.common.CommonObjectsProvider;
import com.fluxnetworks.plugin.common.LanguageHandler.Term;
import com.fluxnetworks.plugin.spigot.FluxPlugin;

import java.util.List;
import java.util.Optional;

public class GetNotificationsCommand extends CommonCommand {

	public GetNotificationsCommand(final CommonObjectsProvider provider) {
		super(provider);
	}

	@Override
	public void execute(final CommandSender sender, final String[] args, final String usage) {
		if (args.length != 0) {
			sender.sendLegacyMessage(usage);
			return;
		}

		if (!sender.isPlayer()) {
			sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTAPLAYER));
			return;
		}

		getScheduler().runAsync(() -> {
			final Optional<FluxAPI> optApi = this.getApi();
			if (!optApi.isPresent()) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTIFICATIONS_OUTPUT_FAIL));
				return;
			}
			final FluxAPI api = optApi.get();

			try {
				final Optional<FluxUser> optional = FluxPlugin.getInstance().getApiProvider().userFromPlayer(api, sender.getUniqueId(),sender.getName());

				if (!optional.isPresent()) {
					sender.sendMessage(getLanguage().getComponent(Term.PLAYER_SELF_NOTREGISTERED));
					return;
				}

				final FluxUser user = optional.get();

				final List<Notification> notifications = user.getNotifications();

				notifications.sort((n1, n2) -> n2.getType().ordinal() - n1.getType().ordinal());

				if (notifications.size() == 0) {
					sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTIFICATIONS_OUTPUT_NO_NOTIFICATIONS));
					return;
				}

				getScheduler().runSync(() -> {
					notifications.forEach((notification) -> {
						sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTIFICATIONS_OUTPUT_NOTIFICATION,
								"url", notification.getUrl(),
								"message", notification.getMessage()));
					});
				});
			} catch (final FluxException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTIFICATIONS_OUTPUT_FAIL));
				getExceptionLogger().logException(e);
			}
		});
	}

}
