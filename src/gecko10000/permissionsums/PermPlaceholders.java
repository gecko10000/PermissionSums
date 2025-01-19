package gecko10000.permissionsums;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class PermPlaceholders extends PlaceholderExpansion {

    private final PermissionSums plugin;

    public PermPlaceholders(PermissionSums plugin) {
        this.plugin = plugin;
        this.register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "permsums";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getPluginMeta().getAuthors().getFirst();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return null;
        return plugin.calculator.getPermValue(player, params);
    }

    @Override
    public @NotNull List<String> getPlaceholders() {
        List<String> templates = plugin.getConfig().getStringList("integer-permissions");
        templates.addAll(plugin.getConfig().getStringList("decimal-permissions"));
        return templates.stream().map(t -> "%" + getIdentifier() + "_" + t + "%").toList();
    }
}
