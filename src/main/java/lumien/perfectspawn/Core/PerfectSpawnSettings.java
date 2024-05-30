
package lumien.perfectspawn.Core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.DimensionManager;

import org.apache.logging.log4j.Level;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import lumien.perfectspawn.Network.MessageHandler;
import lumien.perfectspawn.Network.PerfectSpawnSettingsMessage;
import lumien.perfectspawn.PerfectSpawn;

public class PerfectSpawnSettings {

    private SettingEntry globalSetting;
    private HashMap<String, SettingEntry> worldSettings;
    File worldDictionary;

    public static class SettingEntry {

        int spawnDimension;
        int spawnX;
        int spawnY;
        int spawnZ;
        boolean forceBed;
        boolean exactSpawn;
        boolean spawnProtection;

        public SettingEntry(int spawnDimension, int spawnX, int spawnY, int spawnZ) {
            this.spawnDimension = spawnDimension;

            this.spawnX = spawnX;
            this.spawnY = spawnY;
            this.spawnZ = spawnZ;

            this.forceBed = true;
            this.exactSpawn = true;
            this.spawnProtection = true;
        }

        public boolean isExactSpawn() {
            return this.exactSpawn;
        }

        public boolean forceBed() {
            return this.forceBed;
        }

        public void setExactSpawn(boolean exactSpawn) {
            this.exactSpawn = exactSpawn;
        }

        public void setForceBed(boolean forceBed) {
            this.forceBed = forceBed;
        }

        public int getSpawnDimension() {
            return this.spawnDimension;
        }

        public int getSpawnX() {
            return this.spawnX;
        }

        public int getSpawnY() {
            return this.spawnY;
        }

        public int getSpawnZ() {
            return this.spawnZ;
        }

        public String toString() {
            return "SettingEntry(spawnX=" + this.spawnX
                + ",spawnY="
                + this.spawnY
                + ",spawnZ="
                + this.spawnZ
                + ",spawnDimension="
                + this.spawnDimension;
        }

        public void setSpawnProtection(boolean spawnProtection) {
            this.spawnProtection = spawnProtection;
        }
    }

    public PerfectSpawnSettings() {
        this.globalSetting = null;
        this.worldSettings = new HashMap<String, SettingEntry>();
    }

    public void init() {
        this.globalSetting = loadConfigFile(new File("PerfectSpawn.json"));
    }

    public void reload() {
        init();
        serverStarted();

        SettingEntry se = getValidSettingEntry();
        if (se != null && MinecraftServer.getServer()
            .worldServerForDimension(se.spawnDimension) != null) {
            PSEventHandler.setSpawnPoint(se.spawnDimension, se.spawnX, se.spawnY, se.spawnZ);
        }

        sendPackets();
    }

    private void sendPackets() {
        SettingEntry se = PerfectSpawn.settings.getValidSettingEntry();

        PerfectSpawnSettingsMessage message = null;

        if (se == null) {

            message = new PerfectSpawnSettingsMessage();
        } else {

            message = new PerfectSpawnSettingsMessage(se);
        }

        MessageHandler.INSTANCE.sendToAll((IMessage) message);
    }

    public SettingEntry getValidSettingEntry() {
        if (MinecraftServer.getServer()
            .getServerOwner() != null) {

            String world = MinecraftServer.getServer()
                .getFolderName();
            if (world != null && this.worldSettings.containsKey(world)) {
                return this.worldSettings.get(world);
            }
        }
        if (this.globalSetting != null) {
            return this.globalSetting;
        }

        return null;
    }

    public void serverStarted() {
        File worldDictionary = DimensionManager.getCurrentSaveRootDirectory();
        if (worldDictionary != null) {

            SettingEntry se = loadConfigFile(new File(worldDictionary, "PerfectSpawn.json"));
            if (se != null) {
                this.worldSettings.put(
                    MinecraftServer.getServer()
                        .getFolderName(),
                    se);
            }
        }
    }

    private SettingEntry loadConfigFile(File f) {
        if (!f.exists() || f.isDirectory()) {
            return null;
        }

        JsonParser jsonParser = new JsonParser();
        FileReader fileReader = null;

        try {
            fileReader = new FileReader(f);
        } catch (FileNotFoundException fileNotFound) {

            PerfectSpawn.instance.logger.log(Level.WARN, "Couldn't read " + f.getAbsolutePath());
        }
        if (fileReader != null) {

            JsonElement jsonElement = jsonParser.parse(fileReader);

            try {
                fileReader.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
            JsonObject jsonObject = (JsonObject) jsonElement;

            if (jsonObject.has("spawnDimension") && jsonObject.has("spawnX")
                && jsonObject.has("spawnY")
                && jsonObject.has("spawnZ")) {

                PerfectSpawn.instance.logger.log(Level.INFO, "Loading " + f.getAbsolutePath());

                SettingEntry settingsEntry = new SettingEntry(
                    jsonObject.get("spawnDimension")
                        .getAsInt(),
                    jsonObject.get("spawnX")
                        .getAsInt(),
                    jsonObject.get("spawnY")
                        .getAsInt(),
                    jsonObject.get("spawnZ")
                        .getAsInt());

                if (jsonObject.has("forceBed")) {
                    settingsEntry.setForceBed(
                        jsonObject.get("forceBed")
                            .getAsBoolean());
                }

                if (jsonObject.has("exactSpawn")) {
                    settingsEntry.setExactSpawn(
                        jsonObject.get("exactSpawn")
                            .getAsBoolean());
                }

                if (jsonObject.has("spawnProtection")) {
                    settingsEntry.setSpawnProtection(
                        jsonObject.get("spawnProtection")
                            .getAsBoolean());
                }

                return settingsEntry;
            }

            PerfectSpawn.instance.logger.log(
                Level.WARN,
                "Invalid PerfectSpawn config file: (" + f.getAbsolutePath()
                    + ") It needs spawnDimension,spawnX,spawnY,spawnZ.");
        }

        return null;
    }
}

/*
 * Location: /home/midnight/Downloads/PerfectSpawn-1.1-deobf.jar!/lumien/perfectspawn/Core/PerfectSpawnSettings.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version: 1.1.3
 */
