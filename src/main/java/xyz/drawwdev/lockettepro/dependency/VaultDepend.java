package xyz.drawwdev.lockettepro.dependency;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultDepend implements Dependency {

    private Permission permission = null;

    private String version;

    @Override
    public String getName() {
        return "vault";
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public boolean load() {
        final Plugin vaultPlugin = Bukkit.getPluginManager().getPlugin("Vault");
        if (vaultPlugin == null) return false;
        this.version = vaultPlugin.getDescription().getVersion();
        RegisteredServiceProvider<Permission> registeredServiceProvider = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        if (registeredServiceProvider == null) return false;
        permission = registeredServiceProvider.getProvider();
        return true;
    }

    public boolean isPermissionGroupOf(String line, Player player) {
        if (permission == null) return false;
        String[] groups = permission.getPlayerGroups(player);
        for (String group : groups) {
            if (line.equals("[" + group + "]")) return true;
        }
        return false;
    }

    public Permission getPermission() {
        return permission;
    }
}
