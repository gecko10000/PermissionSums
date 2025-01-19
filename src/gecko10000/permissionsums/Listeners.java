package gecko10000.permissionsums;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Map;
import java.util.Set;

public class Listeners implements Listener {

    private final PermissionSums plugin;

    public Listeners(PermissionSums plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        LuckPermsProvider.get().getEventBus().subscribe(plugin, UserDataRecalculateEvent.class, (e) -> {
            Player player = Bukkit.getPlayer(e.getUser().getUniqueId());
            if (player == null) return;
            Map<String, Boolean> permissions = e.getData().getPermissionData().getPermissionMap();
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                recalculate(player, plugin.calculator.extractPerms(permissions));
            });
        });
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            Bukkit.getOnlinePlayers().forEach(this::recalculate);
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        recalculate(event.getPlayer());
    }

    private void recalculate(Player player) {
        Set<PermissionAttachmentInfo> permissions = player.getEffectivePermissions();
        recalculate(player, plugin.calculator.extractPerms(permissions));
    }

    private void recalculate(Player player, Set<String> permissions) {
        if (!player.isOnline()) return;
        // Remove existing ones.
        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            PermissionAttachment attachment = info.getAttachment();
            if (attachment == null) continue;
            if (attachment.getPlugin() instanceof PermissionSums) {
                player.removeAttachment(attachment);
            }
        }
        for (String finalPerm : plugin.calculator.getSummedPermissions(permissions)) {
            player.addAttachment(plugin).setPermission(finalPerm, true);
        }
    }

}
