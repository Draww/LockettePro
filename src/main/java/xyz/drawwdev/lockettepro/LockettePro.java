package xyz.drawwdev.lockettepro;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.drawwdev.lockettepro.dependency.*;
import xyz.drawwdev.lockettepro.listeners.BlockDebugListener;
import xyz.drawwdev.lockettepro.listeners.BlockEnvironmentListener;
import xyz.drawwdev.lockettepro.listeners.BlockInventoryMoveListener;
import xyz.drawwdev.lockettepro.listeners.BlockPlayerListener;

import java.util.*;

public class LockettePro extends JavaPlugin {

    private static LockettePro instance;
    private final Dependencies dependencies = new Dependencies();
    private final boolean debug = false;

    public void onEnable() {
        instance = this;
        // Version
        if (!this.checkMcVersion()) {
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // Dependency
        if (!this.loadDependencies()) {
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // Read config
        new Config(this);
        // Register Listeners
        // If debug mode is not on, debug listener won't register
        if (debug) this.getServer().getPluginManager().registerEvents(new BlockDebugListener(), this);
        this.getServer().getPluginManager().registerEvents(new BlockPlayerListener(), this);
        this.getServer().getPluginManager().registerEvents(new BlockEnvironmentListener(), this);
        this.getServer().getPluginManager().registerEvents(new BlockInventoryMoveListener(), this);
        // Register Command
        final CommandBase commandBase = new CommandBase();
        final PluginCommand command = this.getServer().getPluginCommand("lockettepro");
        if (command != null) {
            command.setExecutor(commandBase);
            command.setTabCompleter(commandBase);
        }
        // If UUID is not enabled, UUID listener won't register
        if (Config.isUuidEnabled() || Config.isLockExpire()) {
            final ProtocolLibDepend protocolLibDepend = dependencies.get(ProtocolLibDepend.class);
            if (protocolLibDepend != null) {
                protocolLibDepend.setup();
            } else {
                this.getLogger().info("ProtocolLib is not found!");
                this.getLogger().info("UUID & expiracy support requires ProtocolLib, or else signs will be ugly!");
            }
        }
    }

    public void onDisable() {
        final ProtocolLibDepend protocolLibDepend = dependencies.get(ProtocolLibDepend.class);
        if (Config.isUuidEnabled() && protocolLibDepend != null) {
            protocolLibDepend.cleanUp();
        }
    }

    private boolean checkMcVersion() {
        try {
            Material.BARREL.isItem();
        } catch (Exception e) {
            setEnabled(false);
            getLogger().warning("This plugin is not compatible with your server version!");
        }
        String[] serverVersion = Bukkit.getBukkitVersion().split("-");
        String version = serverVersion[0];
        if (version.matches("1.17") || version.matches("1.17.1")) {
            this.getLogger().info("Compatible server version detected: " + version);
            return true;
        } else {
            this.getLogger().info("Incompatible server version detected: " + version + " . Trying to run into 1.17 compatibility mode!");
            return false;
        }
    }

    private boolean loadDependencies() {
        final Set<Dependency> dependenciesSet = new HashSet<>(Arrays.asList(new VaultDepend(), new WorldGuardDepend(), new CoreProtectDepend(), new ProtocolLibDepend()));
        for (final Dependency dependency : dependenciesSet) {
            if (!dependencies.register(dependency) && dependency.isRequired())
                return false;
        }
        return true;
    }

    public static LockettePro getInstance() {
        return instance;
    }

    public Dependencies getDependencies() {
        return dependencies;
    }

    public boolean isDebug() {
        return debug;
    }
}
