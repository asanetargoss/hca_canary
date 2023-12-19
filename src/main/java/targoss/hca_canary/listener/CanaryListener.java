package targoss.hca_canary.listener;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import targoss.hca_canary.HCACanary;
import targoss.hca_canary.network.Spoof;
import targoss.hca_canary.util.UtilNetwork;

public class CanaryListener {
    public static class Canary implements Serializable {
        private static final long serialVersionUID = 4131573234025861921L;
        
        private String canaryName;

        public static ConcurrentHashMap<String, Boolean> canaryLiveness = new ConcurrentHashMap<String, Boolean>();
        public static AtomicBoolean canaryDied = new AtomicBoolean(false);
        
        public Canary() {
            this.canaryName = null;
        }
        
        public Canary(String canaryName) {
            this.canaryName = canaryName;
        }
        
        protected void initLiveness() {
            if (canaryName == null) {
                throw new IllegalStateException("Attempted to init canary without a name");
            }
            
            if (canaryLiveness.containsKey(canaryName)) {
                throw new IllegalStateException("Canary already registered with name '" + canaryName);
            }
            
            canaryLiveness.put(canaryName, true);
        }
        
        private void writeObject(ObjectOutputStream stream) throws IOException {
            initLiveness();
            
            stream.writeObject(canaryName);
        }
        
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            String canary = (String)in.readObject();
            if (canary == null) {
                throw new IllegalStateException("Attempted to kill canary without a name");
            }
            HCACanary.LOGGER.error("Canary check failed: '" + canary + "'");
            canaryLiveness.put(canary, false);
            canaryDied.set(true);
        }
        
        @SuppressWarnings("unused")
        private void readObjectNoData() throws ObjectStreamException { }
        
    }
    
    public static class Client {
        protected boolean canariesRegistered = false;

        @SubscribeEvent(priority=EventPriority.HIGHEST)
        void registerCanaries(AttachCapabilitiesEvent<Entity> event) {
            if (canariesRegistered) {
                return;
            }
            if (!(event.getObject() instanceof EntityPlayerSP)) {
                return;
            }
            canariesRegistered = true;
            HCACanary.LOGGER.info("Registering client canaries");
            
            {
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(0); // 0 = PktSyncConfig
                buf.writeByte(1); // About to append one ObjectInputStream object
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                try {
                    // TODO: Should we close these streams just to be safe?
                    (new DataOutputStream(byteStream)).writeUTF("fakeField");
                    (new ObjectOutputStream(byteStream)).writeObject(new Canary("Astral Sorcery PktSyncConfig ( serializationisbad - please see https://github.com/dogboy21/serializationisbad )"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                byte[] bytes = byteStream.toByteArray();
                try {
                    byteStream.close();
                } catch (IOException iOException) {}
                buf.writeShort(bytes.length);
                buf.writeBytes(bytes);
                
                Spoof.Client.spoofBytes("Astral Sorcery", buf);
            }
            
            {
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(0); // 0 = PacketWatchedUpdateC
                buf.writeInt(-1); // fake entity ID
                
                // ByteBufIO.writeObject (compressed ObjectInputStream. Phew!)
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(byteStream);
                    oos.writeObject(new Canary("RadixCore PacketWatchedUpdateC ( serializationisbad - please see https://github.com/dogboy21/serializationisbad )"));
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                byte[] bytes = byteStream.toByteArray();
                try {
                    byteStream.close();
                } catch (IOException iOException) {}
                byte[] compressedBytes = UtilNetwork.compress(bytes);
                buf.writeInt(compressedBytes.length);
                buf.writeBytes(compressedBytes);
                
                Spoof.Client.spoofBytes("RadixCore", buf);
            }
            
            if (Canary.canaryDied.get()) {
                HCACanary.LOGGER.error("One or more canary checks failed on the client. This generally means a security patch mod did not run as intended, or was disabled by the user. The mod and modpack developers are not responsible for any issues resulting from running a Minecraft server, regardless of whether or not these security mods are installed. If this error appears on a fresh modpack install, open an issue on the issue tracker: https://github.com/asanetargoss/HardcoreAlchemy/issues");
                Minecraft.getMinecraft().shutdown();
            }
        }
    }
    
    protected boolean canariesRegistered = false;
    
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    void registerCanaries(PlayerLoggedInEvent event) {
        if (canariesRegistered) {
            return;
        }
        EntityPlayer player = event.player;
        World world = player.world;
        if (!(world instanceof WorldServer)) {
            // Shouldn't happen
            return;
        }
        
        canariesRegistered = true;
        HCACanary.LOGGER.info("Registering server canaries");
        
        {
            ByteBuf buf = Unpooled.buffer();
            buf.writeByte(0); // 1 = PacketWatchedUpdateS
            buf.writeInt(-1); // fake entity ID
            
            // ByteBufIO.writeObject (compressed ObjectInputStream. Phew!)
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(byteStream);
                oos.writeObject(new Canary("RadixCore PacketWatchedUpdateS ( serializationisbad - please see https://github.com/dogboy21/serializationisbad )"));
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] bytes = byteStream.toByteArray();
            try {
                byteStream.close();
            } catch (IOException iOException) {}
            byte[] compressedBytes = UtilNetwork.compress(bytes);
            buf.writeInt(compressedBytes.length);
            buf.writeBytes(compressedBytes);
            
            Spoof.Client.spoofBytes("RadixCore", buf);
        }
        
        if (Canary.canaryDied.get()) {
            HCACanary.LOGGER.error("One or more canary checks failed on the server. This generally means a security patch mod did not run as intended, or was disabled by the user. The mod and modpack developers are not responsible for any issues resulting from running a Minecraft server, regardless of whether or not these security mods are installed. If this error appears on a fresh modpack install, open an issue on the issue tracker: https://github.com/asanetargoss/HardcoreAlchemy/issues");
            WorldServer worldServer = (WorldServer)world;
            worldServer.getMinecraftServer().initiateShutdown();
        }
        
    }
}
