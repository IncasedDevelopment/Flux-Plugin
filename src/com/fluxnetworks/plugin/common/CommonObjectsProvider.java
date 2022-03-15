package com.fluxnetworks.plugin.common;

import com.fluxnetworks.plugin.common.command.AbstractScheduler;

import net.kyori.adventure.platform.AudienceProvider;

public interface CommonObjectsProvider {

	AbstractScheduler getScheduler();

	LanguageHandler getLanguage();

	ApiProvider getApiProvider();

	AudienceProvider adventure();

	ExceptionLogger getExceptionLogger();

}
