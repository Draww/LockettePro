package xyz.drawwdev.lockettepro;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.drawwdev.lockettepro.dependency.CoreProtectDepend;
import xyz.drawwdev.lockettepro.dependency.ProtocolLibDepend;
import xyz.drawwdev.lockettepro.dependency.VaultDepend;
import xyz.drawwdev.lockettepro.dependency.WorldGuardDepend;

import java.util.ArrayList;
import java.util.List;

public class CommandBase implements CommandExecutor, TabCompleter {

    @SuppressWarnings({"ConstantConditions", "EnhancedSwitchMigration"})
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equals("lockettepro")) {
            if (args.length == 0) {
                Utils.sendMessages(sender, Config.getLang("command-usage"));
            } else {
                // The following commands does not require player
                switch (args[0]) {
                    case "reload":
                        if (sender.hasPermission("lockettepro.reload")) {
                            final ProtocolLibDepend protocolLibDepend = LockettePro.getInstance().getDependencies().get(ProtocolLibDepend.class);
                            if (protocolLibDepend != null) protocolLibDepend.cleanUp();
                            Config.reload();
                            if (Config.isUuidEnabled() && protocolLibDepend != null) {
                                protocolLibDepend.setup();
                            }
                            Utils.sendMessages(sender, Config.getLang("config-reloaded"));
                        } else {
                            Utils.sendMessages(sender, Config.getLang("no-permission"));
                        }
                        return true;
                    case "version":
                        if (sender.hasPermission("lockettepro.version")) {
                            sender.sendMessage(LockettePro.getInstance().getDescription().getFullName());
                        } else {
                            Utils.sendMessages(sender, Config.getLang("no-permission"));
                        }
                        return true;
                    case "debug":
                        // This is not the author debug, this prints out info
                        if (sender.hasPermission("lockettepro.debug")) {
                            sender.sendMessage("LockettePro Debug Message");
                            // Basic
                            sender.sendMessage("LockettePro: " + LockettePro.getInstance().getDescription().getVersion());
                            // Version
                            sender.sendMessage("Bukkit: " + "v" + Bukkit.getServer().getClass().getPackage().getName().split("v")[1]);
                            sender.sendMessage("Server version: " + Bukkit.getVersion());
                            // Config
                            sender.sendMessage("UUID: " + Config.isUuidEnabled());
                            sender.sendMessage("Expire: " + Config.isLockExpire() + " " + (Config.isLockExpire() ? Config.getLockExpireDays() : ""));
                            // ProtocolLib
                            sender.sendMessage("ProtocolLib info:");
                            final ProtocolLibDepend protocolLibDepend = LockettePro.getInstance().getDependencies().get(ProtocolLibDepend.class);
                            if (protocolLibDepend == null) {
                                sender.sendMessage(" - ProtocolLib missing");
                            } else {
                                sender.sendMessage(" - ProtocolLib: " + protocolLibDepend.getVersion());
                            }
                            // Other
                            sender.sendMessage("Linked plugins:");
                            boolean linked = false;
                            final WorldGuardDepend worldGuardDepend = LockettePro.getInstance().getDependencies().get(WorldGuardDepend.class);
                            if (worldGuardDepend != null) {
                                linked = true;
                                sender.sendMessage(" - Worldguard: " + worldGuardDepend.getVersion());
                            }
                            final VaultDepend vaultDepend = LockettePro.getInstance().getDependencies().get(VaultDepend.class);
                            if (vaultDepend != null) {
                                linked = true;
                                sender.sendMessage(" - Vault: " + vaultDepend.getVersion());
                            }
                            final CoreProtectDepend coreProtectDepend = LockettePro.getInstance().getDependencies().get(CoreProtectDepend.class);
                            if (coreProtectDepend != null) {
                                linked = true;
                                sender.sendMessage(" - CoreProtect: " + coreProtectDepend.getVersion());
                            }
                            if (!linked) {
                                sender.sendMessage(" - none");
                            }
                        } else {
                            Utils.sendMessages(sender, Config.getLang("no-permission"));
                        }
                        return true;
                }
                // The following commands requires player
                if (!(sender instanceof Player)) {
                    Utils.sendMessages(sender, Config.getLang("command-usage"));
                    return false;
                }
                final Player player = (Player) sender;
                switch (args[0]) {
                    case "1":
                    case "2":
                    case "3":
                    case "4":
                        if (player.hasPermission("lockettepro.edit")) {
                            StringBuilder message = new StringBuilder();
                            Block block = Utils.getSelectedSign(player);
                            if (block == null) {
                                Utils.sendMessages(player, Config.getLang("no-sign-selected"));
                            } else if (!LocketteProAPI.isSign(block) || !(player.hasPermission("lockettepro.edit.admin") || LocketteProAPI.isOwnerOfSign(block, player))) {
                                Utils.sendMessages(player, Config.getLang("sign-need-reselect"));
                            } else {
                                for (int i = 1; i < args.length; i++) {
                                    message.append(args[i]);
                                }
                                message = new StringBuilder(ChatColor.translateAlternateColorCodes('&', message.toString()));
                                if (!player.hasPermission("lockettepro.admin.edit") && !LockettePro.getInstance().isDebug() && message.length() > 18) {
                                    Utils.sendMessages(player, Config.getLang("line-is-too-long"));
                                    return true;
                                }
                                if (LocketteProAPI.isLockSign(block)) {
                                    switch (args[0]) {
                                        case "1":
                                            if (!LockettePro.getInstance().isDebug() || !player.hasPermission("lockettepro.admin.edit")) {
                                                Utils.sendMessages(player, Config.getLang("cannot-change-this-line"));
                                                break;
                                            }
                                        case "2":
                                            if (!player.hasPermission("lockettepro.admin.edit")) {
                                                Utils.sendMessages(player, Config.getLang("cannot-change-this-line"));
                                                break;
                                            }
                                        case "3":
                                        case "4":
                                            Utils.setSignLine(block, Integer.parseInt(args[0]) - 1, message.toString());
                                            Utils.sendMessages(player, Config.getLang("sign-changed"));
                                            if (Config.isUuidEnabled()) {
                                                Utils.updateUuidByUsername(Utils.getSelectedSign(player), Integer.parseInt(args[0]) - 1);
                                            }
                                            break;
                                    }
                                } else if (LocketteProAPI.isAdditionalSign(block)) {
                                    switch (args[0]) {
                                        case "1":
                                            if (!LockettePro.getInstance().isDebug() || !player.hasPermission("lockettepro.admin.edit")) {
                                                Utils.sendMessages(player, Config.getLang("cannot-change-this-line"));
                                                break;
                                            }
                                        case "2":
                                        case "3":
                                        case "4":
                                            Utils.setSignLine(block, Integer.parseInt(args[0]) - 1, message.toString());
                                            Utils.sendMessages(player, Config.getLang("sign-changed"));
                                            if (Config.isUuidEnabled()) {
                                                Utils.updateUuidByUsername(Utils.getSelectedSign(player), Integer.parseInt(args[0]) - 1);
                                            }
                                            break;
                                    }
                                } else {
                                    Utils.sendMessages(player, Config.getLang("sign-need-reselect"));
                                }
                            }
                        } else {
                            Utils.sendMessages(player, Config.getLang("no-permission"));
                        }
                        break;
                    case "force":
                        if (LockettePro.getInstance().isDebug() && player.hasPermission("lockettepro.debug")) {
                            Utils.setSignLine(Utils.getSelectedSign(player), Integer.parseInt(args[1]), args[2]);
                            break;
                        }
                    case "update":
                        if (LockettePro.getInstance().isDebug() && player.hasPermission("lockettepro.debug")) {
                            Utils.updateSign(Utils.getSelectedSign(player));
                            break;
                        }
                    case "uuid":
                        if (LockettePro.getInstance().isDebug() && player.hasPermission("lockettepro.debug")) {
                            Utils.updateUuidOnSign(Utils.getSelectedSign(player));
                            break;
                        }
                    default:
                        Utils.sendMessages(player, Config.getLang("command-usage"));
                        break;
                }
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> commands = new ArrayList<>();
        commands.add("reload");
        commands.add("version");
        commands.add("1");
        commands.add("2");
        commands.add("3");
        commands.add("4");
        commands.add("uuid");
        commands.add("update");
        commands.add("debug");
        if (args != null && args.length == 1) {
            List<String> list = new ArrayList<>();
            for (String s : commands) {
                if (s.startsWith(args[0])) {
                    list.add(s);
                }
            }
            return list;
        }
        return null;
    }
}
