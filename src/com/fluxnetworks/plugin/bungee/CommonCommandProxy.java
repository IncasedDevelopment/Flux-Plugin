package com.fluxnetworks.plugin.bungee;

import com.fluxnetworks.plugin.common.LanguageHandler.Term;
import com.fluxnetworks.plugin.common.Permission;
import com.fluxnetworks.plugin.common.command.CommonCommand;
import com.fluxnetworks.plugin.common.command.GetNotificationsCommand;
import com.fluxnetworks.plugin.common.command.RegisterCommand;
import com.fluxnetworks.plugin.common.command.ReportCommand;
import com.fluxnetworks.plugin.common.command.UserInfoCommand;
import com.fluxnetworks.plugin.common.command.VerifyCommand;
import com.fluxnetworks.plugin.spigot.Config;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class CommonCommandProxy extends Command {

	static final Map<String, Supplier<CommonCommandProxy>> COMMAND_SUPPLIERS = new HashMap<>();

	static {
		COMMAND_SUPPLIERS.put("get-notifications", () -> createCommand(
				new GetNotificationsCommand(FluxPlugin.getInstance()),
				"get-notifications",
				Term.COMMAND_NOTIFICATIONS_USAGE,
				Permission.COMMAND_GET_NOTIFICATIONS));

		COMMAND_SUPPLIERS.put("register", () -> createCommand(
				new RegisterCommand(FluxPlugin.getInstance()),
				"register",
				Term.COMMAND_REGISTER_USAGE,
				Permission.COMMAND_REGISTER));

		COMMAND_SUPPLIERS.put("report", () -> createCommand(
				new ReportCommand(FluxPlugin.getInstance()),
				"report",
				Term.COMMAND_REPORT_USAGE,
				Permission.COMMAND_REPORT));

		COMMAND_SUPPLIERS.put("user-info", 	() -> createCommand(
				new UserInfoCommand(FluxPlugin.getInstance()),
				"user-info",
				Term.COMMAND_USERINFO_USAGE,
				Permission.COMMAND_USER_INFO));

		COMMAND_SUPPLIERS.put("validate", () -> createCommand(
				new VerifyCommand(FluxPlugin.getInstance()),
				"validate",
				Term.COMMAND_VALIDATE_USAGE,
				Permission.COMMAND_VERIFY));
	}

	private final CommonCommand commonCommand;
	private final String usage;

	private CommonCommandProxy(final CommonCommand commonCommand, final String name, final String usage, final Permission permission, final String[] aliases) {
		super(name, permission.toString(), aliases);
		this.commonCommand = commonCommand;
		this.usage = usage;
	}

	private static CommonCommandProxy createCommand(final CommonCommand commonCommand, final String configName, final Term usage, final Permission permission) {
		return new CommonCommandProxy(commonCommand,
				Config.COMMANDS.getConfig().getString(configName),
				FluxPlugin.getInstance().getLanguage().getLegacyMessage(usage).replace("{command}", Config.COMMANDS.getConfig().getString(configName)),
				permission,
				new String[] {});
	}

	@Override
	public void execute(final CommandSender sender, final String[] args) {
		BungeeCommandSender sender2 = new BungeeCommandSender(sender);

		if (!sender.hasPermission(Objects.requireNonNull(this.getPermission()))) {
			sender2.sendMessage(FluxPlugin.getInstance().getLanguage().getComponent(Term.COMMAND_NO_PERMISSION));
			return;
		}

		this.commonCommand.execute(sender2, args, this.usage);
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
