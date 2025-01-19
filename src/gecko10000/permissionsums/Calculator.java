package gecko10000.permissionsums;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculator {

    private final PermissionSums plugin;

    public Calculator(PermissionSums plugin) {
        this.plugin = plugin;
    }

    private Map<String, Integer> sumIntegerPerms(Set<String> rawPermissions) {
        Set<String> configIntPerms = new HashSet<>(plugin.getConfig().getStringList("integer-permissions"));
        Map<String, Integer> sums = new HashMap<>();
        for (String perm : rawPermissions) {
            int idIndex = perm.lastIndexOf('.');
            // sum.<inner>.id
            String inner = perm.substring(4, idIndex);
            // <permRoot>.<amount>
            int amountIndex = inner.lastIndexOf('.') + 1;
            String permString = inner.substring(0, amountIndex);
            // Ensure it's actually an int perm
            if (!configIntPerms.contains(permString + "<amount>")) continue;
            String amountString = inner.substring(amountIndex);
            try {
                int amount = Integer.parseInt(amountString);
                sums.compute(permString, (k, v) -> {
                    int prev = v == null ? 0 : v;
                    return prev + amount;
                });
            } catch (NumberFormatException e) {
                // the thrown exception in question:
                e.printStackTrace();
            }
        }
        return sums;
    }

    private final Pattern doublePermPattern = Pattern.compile(".*?\\.(\\d+(\\.\\d+)?)");

    private Map<String, Double> sumDecimalPerms(Set<String> rawPermissions) {
        Set<String> configDecimalPerms = new HashSet<>(plugin.getConfig().getStringList("decimal-permissions"));
        Map<String, Double> sums = new HashMap<>();
        for (String perm : rawPermissions) {
            int idIndex = perm.lastIndexOf('.');
            String inner = perm.substring(4, idIndex);
            Matcher matcher = doublePermPattern.matcher(inner);
            if (!matcher.matches()) continue;
            String amountString = matcher.group(1);
            int amountIndex = inner.lastIndexOf(amountString);
            String permString = inner.substring(0, amountIndex);
            if (!configDecimalPerms.contains(permString + "<amount>")) continue;
            try {
                double amount = Double.parseDouble(amountString);
                sums.compute(permString, (k, v) -> {
                    double prev = v == null ? 0 : v;
                    return prev + amount;
                });
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return sums;
    }

    // At this point, rawPermissions is expected to
    // only contain permissions starting with "sum."
    public Set<String> getSummedPermissions(Set<String> rawPermissions) {
        Set<String> finalPermissions = new HashSet<>();
        Map<String, Integer> intSums = sumIntegerPerms(rawPermissions);
        for (Map.Entry<String, Integer> entry : intSums.entrySet()) {
            finalPermissions.add(entry.getKey() + entry.getValue());
        }
        Map<String, Double> decimalSums = sumDecimalPerms(rawPermissions);
        int decimalPrecision = plugin.getConfig().getInt("decimal-precision");
        for (Map.Entry<String, Double> entry : decimalSums.entrySet()) {
            String perm = String.format("%s%." + decimalPrecision + "f", entry.getKey(), entry.getValue());
            finalPermissions.add(perm);
        }
        return finalPermissions;
    }

    public Set<String> extractPerms(Map<String, Boolean> permissions) {
        Set<String> truePermissions = new HashSet<>();
        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
            if (!entry.getValue()) continue;
            String perm = entry.getKey();
            if (!perm.startsWith("sum.")) continue;
            truePermissions.add(perm);
        }
        return truePermissions;
    }

    public Set<String> extractPerms(Set<PermissionAttachmentInfo> permissions) {
        Set<String> truePermissions = new HashSet<>();
        for (PermissionAttachmentInfo info : permissions) {
            if (!info.getValue()) continue;
            String perm = info.getPermission();
            if (!perm.startsWith("sum.")) continue;
            truePermissions.add(perm);
        }
        return truePermissions;
    }

    public String getSummedPerm(Player target, String permission) {
        Set<String> perms = extractPerms(target.getEffectivePermissions());
        Map<String, Integer> intSums = sumIntegerPerms(perms);
        Map<String, Double> decimalSums = sumDecimalPerms(perms);
        int placeholderIndex = permission.lastIndexOf("<amount>");
        String permPrefix = permission.substring(0, placeholderIndex);

        String value = intSums.containsKey(permPrefix) ? intSums.get(permPrefix).toString() :
                decimalSums.containsKey(permPrefix) ? decimalSums.get(permPrefix).toString() : null;
        return value == null ? null : (permPrefix + value);
    }

}
