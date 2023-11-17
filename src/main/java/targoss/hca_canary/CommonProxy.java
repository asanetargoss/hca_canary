package targoss.hca_canary;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import targoss.hca_canary.listener.CanaryListener;

public class CommonProxy {
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new CanaryListener());
    }
}
