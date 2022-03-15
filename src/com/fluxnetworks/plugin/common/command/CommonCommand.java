package com.fluxnetworks.plugin.common.command;

import com.fluxnetworks.java_api.FluxAPI;
import com.fluxnetworks.plugin.common.ApiProvider;
import com.fluxnetworks.plugin.common.CommonObjectsProvider;
import com.fluxnetworks.plugin.common.ExceptionLogger;
import com.fluxnetworks.plugin.common.LanguageHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class CommonCommand {

	private final CommonObjectsProvider provider;

	public CommonCommand(final CommonObjectsProvider provider) {
		this.provider = provider;
	}

	protected @NotNull AbstractScheduler getScheduler() {
		return this.provider.getScheduler();
	}

	protected @NotNull LanguageHandler getLanguage() {
		return this.provider.getLanguage();
	}

	protected @NotNull ApiProvider getApiProvider(){
		return this.provider.getApiProvider();
	}

	protected @NotNull Optional<FluxAPI> getApi(){
		return this.getApiProvider().getFluxApi();
	}

	protected @NotNull ExceptionLogger getExceptionLogger() { return this.provider.getExceptionLogger(); }

	public abstract void execute(CommandSender sender, String[] args, String usage);

}
