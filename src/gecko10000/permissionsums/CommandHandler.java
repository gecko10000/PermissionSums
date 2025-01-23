package gecko10000.permissionsums;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
        sender.sendMessage(mm.deserialize("<red>Usage: /permsums check <permission> [player]"));
        sender.sendMessage(mm.deserialize("<red>Usage: /permsums add <permission> <player>"));
    }

    private final LuckPerms luckPerms = LuckPermsProvider.get();

    private CompletableFuture<Void> addNode(Player player, String node) {
        return luckPerms.getUserManager().modifyUser(player.getUniqueId(), u -> {
            u.data().add(Node.builder(node).build());
        });
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
        if (args[0].equals("add") && args.length >= 3 && sender.hasPermission("permissionsums.add")) {
            Player targetPlayer = Bukkit.getPlayer(args[2]);
            if (targetPlayer != null) {
                String permission = "sum." + args[1] + "." + UUID.randomUUID();
                Component successMessage = mm.deserialize("<green>Gave <player> the permission \"<perm>\".",
                        Placeholder.unparsed("perm", permission),
                        Placeholder.component("player", targetPlayer.name()));
                addNode(targetPlayer, permission)
                        .thenRun(() -> sender.sendMessage(successMessage));
                return true;
            }
        }
        showUsage(sender);
        return true;
    }

    private List<String> matching(String arg, String... possible) {
        return Arrays.stream(possible).filter(s -> s.startsWith(arg)).toList();
    }

    private List<String> matchingPerms(String arg) {
        List<String> allPerms = new ArrayList<>(plugin.getConfig().getStringList("integer-permissions"));
        allPerms.addAll(plugin.getConfig().getStringList("decimal-permissions"));
        return matching(arg, allPerms.toArray(String[]::new));
    }

    private List<String> matchingPlayers(String arg) {
        return matching(arg, Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new));
    }

    private List<String> matchingReadyPerms(String arg) {
        List<String> matchingPerms = matchingPerms(arg);
        return matchingPerms.stream().map(s -> s.replace("<amount>", "")).toList();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) return List.of();
        if (args.length == 1) return matching(args[0], "reload", "check", "add");
        if (args[0].equals("check") && args.length == 2) {
            return matchingPerms(args[1]);
        }
        if (args[0].equals("check") && args.length == 3) {
            return matchingPlayers(args[2]);
        }
        if (args[0].equals("add") && args.length == 2) {
            return matchingReadyPerms(args[1]);
        }
        if (args[0].equals("add") && args.length == 3) {
            return matchingPlayers(args[2]);
        }
        return List.of();
    }
}
