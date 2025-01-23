package gecko10000.permissionsums;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final PermissionSums plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public CommandHandler(PermissionSums plugin) {
        this.plugin = plugin;
        PluginCommand command = plugin.getCommand("permissionsums");
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    private void showUsage(CommandSender sender) {
        sender.sendMessage(mm.deserialize("<red>Usage: /permsums reload"));
        sender.sendMessage(mm.deserialize("<red>or /permsums check <permission> [player]"));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            showUsage(sender);
            return true;
        }
        if (args[0].equals("reload") && sender.hasPermission("permissionsums.reload")) {
            plugin.reloadConfig();
            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.listeners.recalculate(player);
            }
            sender.sendMessage(mm.deserialize("<green>Config reloaded."));
            return true;
        }
        if (args[0].equals("check") && args.length >= 2 && sender.hasPermission("permissionsums.check")) {
            Player target = args.length >= 3 ? Bukkit.getPlayer(args[2]) : sender instanceof Player ? (Player) sender : null;
            if (target == null) {
                sender.sendMessage(mm.deserialize("<red>Invalid target."));
                return true;
            }
            String summed = plugin.calculator.getSummedPerm(target, args[1]);
            if (summed == null) {
                sender.sendMessage(mm.deserialize("<red>Invalid permission <perm>.", Placeholder.unparsed("perm", args[1])));
            } else {
                sender.sendMessage(mm.deserialize("<green><player>'s current permission is <perm>.",
                        Placeholder.component("player", target.name()),
                        Placeholder.unparsed("perm", summed)
                ));
            }
            return true;
        }
        showUsage(sender);
        return true;
    }

    private List<String> matching(String arg, String... possible) {
        return Arrays.stream(possible).filter(s -> s.startsWith(arg)).toList();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) return List.of();
        if (args.length == 1) return matching(args[0], "reload", "check");
        if (args[0].equals("check") && args.length == 2) {
            List<String> allPerms = new ArrayList<>(plugin.getConfig().getStringList("integer-permissions"));
            allPerms.addAll(plugin.getConfig().getStringList("decimal-permissions"));
            return matching(args[1], allPerms.toArray(String[]::new));
        }
        if (args[0].equals("check") && args.length == 3) {
            return matching(args[2], Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new));
        }
        return List.of();
    }
}
