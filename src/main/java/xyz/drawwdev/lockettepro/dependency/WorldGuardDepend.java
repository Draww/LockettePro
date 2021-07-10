package xyz.drawwdev.lockettepro.dependency;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xyz.drawwdev.lockettepro.Config;

public class WorldGuardDepend implements Dependency {

    private WorldGuardPlugin worldGuard = null;

    private String version;

    @Override
    public String getName() {
        return "worldguard";
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
        if (!Config.worldguard) return true;
        Plugin worldGuardPlugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuardPlugin == null) return false;
        if (!(worldGuardPlugin instanceof WorldGuardPlugin)) return false;
        worldGuard = (WorldGuardPlugin) worldGuardPlugin;
        this.version = worldGuardPlugin.getDescription().getVersion();
        return true;
    }

    public boolean isProtectedFrom(Block block, Player player){
        if (worldGuard == null) return false;
        return !worldGuard.createProtectionQuery().testBlockPlace(player, block.getLocation(), block.getType());
    }

    public WorldGuardPlugin getWorldGuard() {
        return worldGuard;
    }
}
