package com.fluxnetworks.plugin.common.command;

import com.fluxnetworks.java_api.FluxAPI;
import com.fluxnetworks.java_api.FluxException;
import com.fluxnetworks.java_api.exception.InvalidValidateCodeException;
import com.fluxnetworks.plugin.common.CommonObjectsProvider;
import com.fluxnetworks.plugin.common.LanguageHandler.Term;
import com.fluxnetworks.plugin.spigot.FluxPlugin;

import java.util.Optional;

public class VerifyCommand extends CommonCommand {

	public VerifyCommand(final CommonObjectsProvider provider) {
		super(provider);
	}

	@Override
	public void execute(final CommandSender sender, final String[] args, final String usage) {
		if (args.length != 1) {
			sender.sendLegacyMessage(usage);
			return;
		}

		if (!sender.isPlayer()) {
			sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTAPLAYER));
			return;
		}

		FluxPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(FluxPlugin.getInstance(), () -> {
			final Optional<FluxAPI> optApi = this.getApi();
			if (!optApi.isPresent()) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTIFICATIONS_OUTPUT_FAIL));
				return;
			}
			final FluxAPI api = optApi.get();

			try {
				final String code = args[0];
				api.verifyMinecraft(code, sender.getUniqueId(), sender.getName());
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_VALIDATE_OUTPUT_SUCCESS));
			} catch (final InvalidValidateCodeException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_VALIDATE_OUTPUT_FAIL_INVALIDCODE));
			} catch (final FluxException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_VALIDATE_OUTPUT_FAIL_GENERIC));
				getExceptionLogger().logException(e);
			}
		});
	}

}
