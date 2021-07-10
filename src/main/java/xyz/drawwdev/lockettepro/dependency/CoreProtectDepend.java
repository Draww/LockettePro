package xyz.drawwdev.lockettepro.dependency;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import xyz.drawwdev.lockettepro.Config;
import xyz.drawwdev.lockettepro.LockettePro;

public class CoreProtectDepend implements Dependency {

    private CoreProtectAPI coreProtectAPI = null;

    private String version;

    @Override
    public String getName() {
        return "coreprotect";
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
        if (!Config.coreprotect) return true;
        try {
            coreProtectAPI = CoreProtect.getInstance().getAPI();
            if (!coreProtectAPI.isEnabled()) {
                coreProtectAPI = null;
                LockettePro.getInstance().getLogger().warning("CoreProtect API is not enabled!");
                return false;
            } else {
                this.version = CoreProtect.getInstance().getDescription().getVersion();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void logPlacement(Player player, Block block) {
        if (coreProtectAPI == null || !coreProtectAPI.isEnabled()) return;
        coreProtectAPI.logPlacement(player.getName(), block.getLocation(), block.getType(), block.getBlockData());
    }

    public CoreProtectAPI getCoreProtectAPI() {
        return coreProtectAPI;
    }
}
