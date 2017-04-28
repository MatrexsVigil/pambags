package nl.lang2619.bagginses.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import nl.lang2619.bagginses.Bagginses;
import nl.lang2619.bagginses.config.ConfigHandler;
import nl.lang2619.bagginses.config.ModConfig;
import nl.lang2619.bagginses.event.*;
import nl.lang2619.bagginses.helpers.KeybindHandler;
import nl.lang2619.bagginses.helpers.Messages.OpenBagMessage;
import nl.lang2619.bagginses.items.ModItems;
import nl.lang2619.bagginses.references.BlockList;
import nl.lang2619.bagginses.references.Defaults;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by Tim on 8/24/2014.
 */
public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        //Stops "Failed to send analytics event data"
        System.setErr(new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        }));

        Bagginses.path = event.getModConfigurationDirectory().getAbsolutePath() + File.separator + Defaults.MODID.toLowerCase() + File.separator;
        ConfigHandler.init(Bagginses.path);

        ModItems.init();
    }

    public void init(FMLInitializationEvent event) {
        registerEvents();
        NetworkRegistry.INSTANCE.registerGuiHandler(Bagginses.instance, Bagginses.guiHandler);

        Bagginses.INSTANCE.registerMessage(OpenBagMessage.MyMessageHandler.class, OpenBagMessage.class, 1, Side.SERVER);
    }

    public void postInit(FMLPostInitializationEvent event) {
        BlockList.addFromString(ModConfig.seed, "seed");
        BlockList.addFromString(ModConfig.sapling, "sapling");
        BlockList.addFromString(ModConfig.crop, "crop");

    }

    private void registerEvents(){
        //Closing container on item toss
        MinecraftForge.EVENT_BUS.register(new ItemEvent());
        //Showing allowed/not allowed in bag
        MinecraftForge.EVENT_BUS.register(new TooltipEventHandler());
        //On player death and respawn
        MinecraftForge.EVENT_BUS.register(new SoulBoundEventHandler());
        //For the pickup events
        MinecraftForge.EVENT_BUS.register(new ItemDropEvent());

    }

}
