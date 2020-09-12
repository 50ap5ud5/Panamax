package me.soapsuds.panamax;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.soapsuds.panamax.client.ClientEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("panamax")
public class Panamax {
	
	public static final String MODID = "panamax";
	public static final String NAME = "Panamax";
	
	public static Logger LOGGER = LogManager.getLogger(NAME);


	public Panamax() {
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
//		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PConfig.CONFIG_SPEC);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient));
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onNewRegistries(RegistryEvent.NewRegistry e) {
		
	}
	
	private void setup(final FMLCommonSetupEvent event) {

	}

	private void setupClient(final FMLClientSetupEvent event) {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> ClientEvents::registerClientObjects);
	}

	@SubscribeEvent
	public void gatherData(GatherDataEvent e) {
	
	}
}
