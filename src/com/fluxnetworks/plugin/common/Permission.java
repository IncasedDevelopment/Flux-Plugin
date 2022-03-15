package com.fluxnetworks.plugin.common;

import org.jetbrains.annotations.NotNull;

public enum Permission {

	COMMAND_GET_NOTIFICATIONS("fluxnetworks.command.getnotifications"),
	COMMAND_REGISTER("fluxnetworks.command.register"),
	COMMAND_REPORT("fluxnetworks.command.report"),
	COMMAND_SET_GROUP("fluxnetworks.command.setgroup"),
	COMMAND_USER_INFO("fluxnetworks.command.userinfo"),
	COMMAND_VERIFY("fluxnetworks.command.verify"),

	COMMAND_FLUX("fluxnetworks.command.flux"),

	;

	private final @NotNull String permissionString;

	Permission(final @NotNull String permissionString){
		this.permissionString = permissionString;
	}

	@Override
	public @NotNull String toString() {
		return this.permissionString;
	}

}
