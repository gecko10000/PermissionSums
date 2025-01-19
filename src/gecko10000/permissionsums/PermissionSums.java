package gecko10000.permissionsums;

import org.bukkit.plugin.java.JavaPlugin;

public class PermissionSums extends JavaPlugin {

    public Calculator calculator;

    @Override
    public void onEnable() {
        reloadConfig();
        calculator = new Calculator(this);
        new Listeners(this);
        new CommandHandler(this);
    }

    @Override
    public void reloadConfig() {
        saveDefaultConfig();
        super.reloadConfig();
    }

}
