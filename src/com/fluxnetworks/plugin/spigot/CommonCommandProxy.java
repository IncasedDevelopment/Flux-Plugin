package com.fluxnetworks.plugin.spigot;

import com.fluxnetworks.plugin.common.LanguageHandler.Term;
import com.fluxnetworks.plugin.common.Permission;
import com.fluxnetworks.plugin.common.command.CommonCommand;
import com.fluxnetworks.plugin.common.command.GetNotificationsCommand;
import com.fluxnetworks.plugin.common.command.RegisterCommand;
import com.fluxnetworks.plugin.common.command.ReportCommand;
import com.fluxnetworks.plugin.common.command.UserInfoCommand;
import com.fluxnetworks.plugin.common.command.VerifyCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class CommonCommandProxy extends Command {

	static final Map<String, Supplier<CommonCommandProxy>> COMMAND_SUPPLIERS = new HashMap<>();

	static {
		COMMAND_SUPPLIERS.put("get-notifications", () -> createCommand(
				new GetNotificationsCommand(FluxPlugin.getInstance()),
				"get-notifications",
				Term.COMMAND_NOTIFICATIONS_DESCRIPTION,
				Term.COMMAND_NOTIFICATIONS_USAGE,
				Permission.COMMAND_GET_NOTIFICATIONS));

		COMMAND_SUPPLIERS.put("register", () -> createCommand(
				new RegisterCommand(FluxPlugin.getInstance()),
				"register",
				Term.COMMAND_REGISTER_DESCRIPTION,
				Term.COMMAND_REGISTER_USAGE,
				Permission.COMMAND_REGISTER));

		COMMAND_SUPPLIERS.put("report", () -> createCommand(
				new ReportCommand(FluxPlugin.getInstance()),
				"report",
				Term.COMMAND_REPORT_DESCRIPTION,
				Term.COMMAND_REPORT_USAGE,
				Permission.COMMAND_REPORT));

		COMMAND_SUPPLIERS.put("user-info", 	() -> createCommand(
				new UserInfoCommand(FluxPlugin.getInstance()),
				"user-info",
				Term.COMMAND_USERINFO_DESCRIPTION,
				Term.COMMAND_USERINFO_USAGE,
				Permission.COMMAND_USER_INFO));

		COMMAND_SUPPLIERS.put("verify", () -> createCommand(
				new VerifyCommand(FluxPlugin.getInstance()),
				"verify",
				Term.COMMAND_VALIDATE_DESCRIPTION,
				Term.COMMAND_VALIDATE_USAGE,
				Permission.COMMAND_VERIFY));
	}

	private final CommonCommand commonCommand;

	private CommonCommandProxy(final CommonCommand commonCommand, final String name, final String description, final String usage, final Permission permission, final List<String> aliases) {
		super(name, description, usage, aliases);
		this.setPermission(permission.toString());
		this.commonCommand = commonCommand;
	}

	private static CommonCommandProxy createCommand(final CommonCommand commonCommand, final String configName, final Term description, final Term usage, final Permission permission) {
		return new CommonCommandProxy(commonCommand,
				Config.COMMANDS.getConfig().getString(configName),
				FluxPlugin.getInstance().getLanguage().getLegacyMessage(description),
				FluxPlugin.getInstance().getLanguage().getLegacyMessage(usage).replace("{command}", Config.COMMANDS.getConfig().getString(configName)),
				permission,
				Collections.emptyList());
	}

	@Override
	public boolean execute(final CommandSender sender, final String commandLabel, final String[] args) {
		final SpigotCommandSender sender2 = new SpigotCommandSender(sender);

		if (!sender.hasPermission(Objects.requireNonNull(this.getPermission()))) {
			sender2.sendMessage(FluxPlugin.getInstance().getLanguage().getComponent(Term.COMMAND_NO_PERMISSION));
			return true;
		}

		this.commonCommand.execute(sender2, args, this.getUsage());
		return true;
	}

//	private final String usageMessage;
//	private final Permission permission;
//
//	protected Command(final String name, final Term description, final Term usage, final Permission permission) {
//		super(Config.COMMANDS.getConfig().getString(name),
//				FluxPlugin.getInstance().getLanguageHandler().getMessage(description),
//				FluxPlugin.getInstance().getLanguageHandler().getMessage(usage, "command", Config.COMMANDS.getConfig().getString(name)),
//				Collections.emptyList());
//		this.usageMessage = FluxPlugin.getInstance().getLanguageHandler().getMessage(usage, "command", Config.COMMANDS.getConfig().getString(name));
//		this.permission = permission;
//	}
//
//	@Override
//	public FluxPlugin getPlugin() {
//		return FluxPlugin.getInstance();
//	}
//
//	public String getUsageWithoutSlash() {
//		return this.usageMessage;
//	}
//
//	@Override
//	public boolean execute(final CommandSender sender, final String label, final String[] args) {
//		if (!this.permission.hasPermission(sender)) {
//			FluxPlugin.getInstance().getLanguageHandler().send(Term.COMMAND_NO_PERMISSION, sender);
//			return true;
//		}
//
//		final boolean success = execute(sender, args);
//
//		if (!success) {
//			sender.sendMessage(this.getUsage());
//		}
//
//		return success;
//	}
//
//	public abstract boolean execute(CommandSender sender, String[] args);

}
