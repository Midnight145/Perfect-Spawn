
package lumien.perfectspawn.Core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import lumien.perfectspawn.PerfectSpawn;

public class CoreHandler {

    static boolean testingRespawnDimension = false;

    public static int getRespawnDimension(WorldProvider provider, EntityPlayerMP player) {
        if (!testingRespawnDimension) {

            PerfectSpawnSettings.SettingEntry se = null;
            if (provider.worldObj.isRemote) {

                se = PerfectSpawnClientHandler.currentServerSettings;
            } else {

                se = PerfectSpawn.settings.getValidSettingEntry();
            }

            if (se != null) {

                testingRespawnDimension = true;
                int normalRespawn = provider.getRespawnDimension(player);
                testingRespawnDimension = false;
                if (normalRespawn == 0) {
                    return se.spawnDimension;
                }
            }
        }
        return -126;
    }

    public static boolean canWakeUp(EntityPlayer player) {
        PerfectSpawnSettings.SettingEntry se = null;
        if (player.worldObj.isRemote) {

            se = PerfectSpawnClientHandler.currentServerSettings;
        } else {

            se = PerfectSpawn.settings.getValidSettingEntry();
        }

        if (se != null) {
            if (se.forceBed && player.worldObj.provider.dimensionId == se.spawnDimension
                && !player.worldObj.provider.isSurfaceWorld()) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBlockNotProtectedByDimension(int dimension) {
        return !isBlockProtectedByDimension(dimension);
    }

    public static boolean isBlockProtectedByDimension(int dimension) {
        WorldServer worldServer = DimensionManager.getWorld(dimension);
        PerfectSpawnSettings.SettingEntry se = null;
        if (((World) worldServer).isRemote) {

            se = PerfectSpawnClientHandler.currentServerSettings;
        } else {

            se = PerfectSpawn.settings.getValidSettingEntry();
        }

        if (se == null || !se.spawnProtection) {
            return (dimension == 0);
        }

        return (se.spawnDimension == dimension);
    }

    public static ChunkCoordinates getRandomizedSpawnPoint(WorldProvider provider) {
        PerfectSpawnSettings.SettingEntry se = null;
        if (provider.worldObj.isRemote) {

            se = PerfectSpawnClientHandler.currentServerSettings;
        } else {

            se = PerfectSpawn.settings.getValidSettingEntry();
        }

        if (se != null && se.exactSpawn && se.spawnDimension == provider.dimensionId) {
            return provider.getSpawnPoint();
        }
        return null;
    }

    public static int canRespawnHere(WorldProvider provider) {
        PerfectSpawnSettings.SettingEntry se = null;
        if (provider.worldObj.isRemote) {

            se = PerfectSpawnClientHandler.currentServerSettings;
        } else {

            se = PerfectSpawn.settings.getValidSettingEntry();
        }

        if (se != null) {

            if (provider.dimensionId == se.spawnDimension) {
                return 1;
            }
            if (provider.dimensionId == 0) {
                return 0;
            }
        }

        return -1;
    }

    public static int isSurfaceWorld(WorldProvider provider) {
        return -126;
    }
}

/*
 * Location: /home/midnight/Downloads/PerfectSpawn-1.1-deobf.jar!/lumien/perfectspawn/Core/CoreHandler.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version: 1.1.3
 */
