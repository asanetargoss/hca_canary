package targoss.hca_canary;

import java.util.Set;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = HCACanary.MOD_ID, version = HCACanary.VERSION, acceptedMinecraftVersions = HCACanary.MC_VERSIONS)
public class HCACanary
{
    public static final String MOD_ID = "hca_canary";
    public static final String VERSION = "@HCA_CANARY_VERSION@";
    public static final String MC_VERSIONS = "[1.10.2]";
    public static final String CLIENT_PROXY = "targoss.hca_canary.ClientProxy";
    public static final String COMMON_PROXY = "targoss.hca_canary.CommonProxy";
    
    @Mod.Instance(HCACanary.MOD_ID)
    public static HCACanary INSTANCE;
    
    @SidedProxy(modId=MOD_ID, clientSide=CLIENT_PROXY, serverSide=COMMON_PROXY)
    public static CommonProxy proxy;
    
    public static Logger LOGGER = null;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }
    
    @EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        LOGGER.trace("Enumerating network channels");
        for (Side side : Side.values()) {
            Set<String> channelNames = NetworkRegistry.INSTANCE.channelNamesFor(side);
            for (String channelName : channelNames) {
                LOGGER.trace("Side: " + side.toString() + ", channel name: " + channelName);
            }
        }
        LOGGER.trace("Done enumerating network channels");
    }
}
