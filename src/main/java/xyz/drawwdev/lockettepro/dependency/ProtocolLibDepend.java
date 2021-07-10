package xyz.drawwdev.lockettepro.dependency;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xyz.drawwdev.lockettepro.Config;
import xyz.drawwdev.lockettepro.LockettePro;
import xyz.drawwdev.lockettepro.LocketteProAPI;
import xyz.drawwdev.lockettepro.Utils;

import java.util.List;

public class ProtocolLibDepend implements Dependency {

    private ProtocolLib protocolLib = null;

    private String version;

    @Override
    public String getName() {
        return "protocollib";
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
        if (!Config.protocollib) return true;
        Plugin protocolLibPlugin = Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib");
        if (protocolLibPlugin == null) return false;
        if (!(protocolLibPlugin instanceof ProtocolLib)) return false;
        protocolLib = (ProtocolLib) protocolLibPlugin;
        this.version = protocolLibPlugin.getDescription().getVersion();
        return true;
    }

    public void setup() {
        if (Config.protocollib) return;
        this.addTileEntityDataListener();
        this.addMapChunkListener();
    }

    public void cleanUp() {
        if (protocolLib == null) return;
        try {
            ProtocolLibrary.getProtocolManager().removePacketListeners(LockettePro.getInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addTileEntityDataListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(LockettePro.getInstance(), ListenerPriority.LOW, PacketType.Play.Server.TILE_ENTITY_DATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                if (packet.getIntegers().read(0) != 9) return;
                NbtCompound nbtcompound = (NbtCompound) packet.getNbtModifier().read(0);
                onSignSend(event.getPlayer(), nbtcompound);
            }
        });
    }

    private void addMapChunkListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(LockettePro.getInstance(), ListenerPriority.LOW, PacketType.Play.Server.MAP_CHUNK) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                List<?> tileentitydatas = packet.getSpecificModifier(List.class).read(0);
                for (Object tileentitydata : tileentitydatas) {
                    NbtCompound nbtcompound = NbtFactory.fromNMSCompound(tileentitydata);
                    if (!"minecraft:sign".equals(nbtcompound.getString("id"))) continue;
                    onSignSend(event.getPlayer(), nbtcompound);
                }
            }
        });
    }

    public static void onSignSend(@SuppressWarnings("unused") final Player player, final NbtCompound nbtcompound) {
        String raw_line1 = nbtcompound.getString("Text1");
        if (LocketteProAPI.isLockStringOrAdditionalString(Utils.getSignLineFromUnknown(raw_line1))) {
            // Private line
            String line1 = Utils.getSignLineFromUnknown(nbtcompound.getString("Text1"));
            if (LocketteProAPI.isLineExpired(line1)) {
                nbtcompound.put("Text1", WrappedChatComponent.fromText(Config.getLockExpireString()).getJson());
            } else {
                nbtcompound.put("Text1", WrappedChatComponent.fromText(Utils.StripSharpSign(line1)).getJson());
            }
            // Other line
            for (int i = 2; i <= 4; i++) {
                String line = Utils.getSignLineFromUnknown(nbtcompound.getString("Text" + i));
                if (Utils.isUsernameUuidLine(line)) {
                    nbtcompound.put("Text" + i, WrappedChatComponent.fromText(Utils.getUsernameFromLine(line)).getJson());
                }
            }
        }
    }

    public ProtocolLib getProtocolLib() {
        return protocolLib;
    }
}
