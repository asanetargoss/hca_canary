package targoss.hca_canary;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import targoss.hca_canary.listener.CanaryListener;

public class ClientProxy extends CommonProxy {
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        
        MinecraftForge.EVENT_BUS.register(new CanaryListener.Client());
    }
}
