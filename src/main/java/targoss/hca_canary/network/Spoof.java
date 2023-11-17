package targoss.hca_canary.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import targoss.hca_canary.HCACanary;

public class Spoof {
    public static class Client {
        public static void spoofBytes(String channelName, ByteBuf buf) {
            if (FMLCommonHandler.instance().getSide() != Side.CLIENT) {
                throw new IllegalStateException("Spoofing client packets from wrong thread");
            }
            
            FMLEmbeddedChannel channel = NetworkRegistry.INSTANCE.getChannel(channelName, Side.CLIENT);
            SPacketCustomPayload payload = new SPacketCustomPayload(channelName, new PacketBuffer(buf));
            FMLProxyPacket proxyPacket = new FMLProxyPacket(payload);
            try {
                channel.writeInbound(proxyPacket);
            } catch (Throwable e) {
                HCACanary.LOGGER.debug("The following exception was caught while running a canary packet spoof. This is most likely normal and the exception can safely be ignored.", e);
            }
        }
    }
    public static class Server {
        public static void spoofBytes(String channelName, ByteBuf buf) {
            if (FMLCommonHandler.instance().getSide() != Side.SERVER) {
                throw new IllegalStateException("Spoofing server packets from wrong thread");
            }

            // TODO: Server sends a packet to itself, using fake player. See if that works, using our own test network packet
            // TODO: Once we confirm it works (or we find a workaround), build framework for sending spoofed packets to arbitrary channels, and confirm that spoofed packets fail to be deserialized via ObjectInputStream
        }
    }
}
