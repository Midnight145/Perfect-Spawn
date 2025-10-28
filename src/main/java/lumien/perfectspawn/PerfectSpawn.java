
package lumien.perfectspawn;

import java.util.Map;

import net.minecraft.command.ICommand;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;
import lumien.perfectspawn.Core.PSEventHandler;
import lumien.perfectspawn.Core.PerfectSpawnCommand;
import lumien.perfectspawn.Core.PerfectSpawnSettings;
import lumien.perfectspawn.Network.MessageHandler;

@Mod(modid = PerfectSpawn.MOD_ID, name = PerfectSpawn.MOD_NAME, version = PerfectSpawn.MOD_VERSION)
public class PerfectSpawn {

    public static final String MOD_ID = "PerfectSpawn";
    public static final String MOD_NAME = "PerfectSpawn";
    public static final String MOD_VERSION = "1.1.2";
    public Logger logger;
    public boolean enabled;
    @Instance(PerfectSpawn.MOD_ID)
    public static PerfectSpawn instance;
    public static PerfectSpawnSettings settings;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        this.logger = event.getModLog();
    }

    @NetworkCheckHandler
    public boolean checkRemote(Map<String, String> mods, Side remoteSide) {
        return true;
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        settings = new PerfectSpawnSettings();
        settings.init();

        PSEventHandler psh = new PSEventHandler();
        MinecraftForge.EVENT_BUS.register(psh);
        FMLCommonHandler.instance()
            .bus()
            .register(psh);

        MessageHandler.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {}

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand((ICommand) new PerfectSpawnCommand());
    }

    @EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        settings.serverStarted();
    }
}

/*
 * Location: /home/midnight/Downloads/PerfectSpawn-1.1-deobf.jar!/lumien/perfectspawn/PerfectSpawn.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version: 1.1.3
 */
