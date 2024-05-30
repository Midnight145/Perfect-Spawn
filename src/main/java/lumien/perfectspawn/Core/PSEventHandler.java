package lumien.perfectspawn.Core;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.WorldEvent;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import lumien.perfectspawn.Network.MessageHandler;
import lumien.perfectspawn.Network.PerfectSpawnSettingsMessage;
import lumien.perfectspawn.PerfectSpawn;
import lumien.perfectspawn.Transformer.MCPNames;

public class PSEventHandler {

    Field sleepTimer;
    Field sleeping;
    public static final String NBT_KEY = "perfectspawnJoined";

    public PSEventHandler() {
        try {
            this.sleepTimer = EntityPlayer.class.getDeclaredField(MCPNames.field("field_71076_b"));
            this.sleepTimer.setAccessible(true);

            this.sleeping = EntityPlayer.class.getDeclaredField(MCPNames.field("field_71083_bS"));
            this.sleeping.setAccessible(true);
        } catch (NoSuchFieldException nsf) {

            nsf.printStackTrace();
        }
    }

    @SubscribeEvent
    public void worldLoaded(WorldEvent.Load event) {
        if (!event.world.isRemote) {

            PerfectSpawnSettings.SettingEntry se = PerfectSpawn.settings.getValidSettingEntry();
            if (se != null && se.spawnDimension == event.world.provider.dimensionId) {

                setSpawnPoint(se.spawnDimension, se.spawnX, se.spawnY, se.spawnZ);
            }
        }
    }

    @SubscribeEvent
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        PerfectSpawnSettings.SettingEntry se = PerfectSpawn.settings.getValidSettingEntry();

        PerfectSpawnSettingsMessage message = null;

        if (se == null) {

            message = new PerfectSpawnSettingsMessage();
        } else {

            message = new PerfectSpawnSettingsMessage(se);
        }

        MessageHandler.INSTANCE.sendTo((IMessage) message, (EntityPlayerMP) event.player);

        if (!event.player.worldObj.isRemote) {

            NBTTagCompound persistent, data = event.player.getEntityData();

            if (!data.hasKey("PlayerPersisted")) {

                data.setTag("PlayerPersisted", (NBTBase) (persistent = new NBTTagCompound()));
            } else {

                persistent = data.getCompoundTag("PlayerPersisted");
            }

            if (!persistent.hasKey("perfectspawnJoined")) {

                persistent.setBoolean("perfectspawnJoined", true);
                if (se != null) {

                    if (se.spawnDimension != event.player.dimension) {
                        MinecraftServer.getServer()
                            .getConfigurationManager()
                            .transferPlayerToDimension(
                                (EntityPlayerMP) event.player,
                                se.spawnDimension,
                                new PerfectSpawnTeleporter(
                                    MinecraftServer.getServer()
                                        .worldServerForDimension(se.spawnDimension)));
                    }
                    ((EntityPlayerMP) event.player).playerNetServerHandler.setPlayerLocation(
                        se.spawnX + 0.5D,
                        se.spawnY,
                        se.spawnZ + 0.5D,
                        event.player.cameraYaw,
                        event.player.cameraPitch);
                    event.player.getEntityData()
                        .setBoolean("psjoined", true);
                }
            }
        }
    }

    @SubscribeEvent
    public void sleepInBed(PlayerSleepInBedEvent event) {
        PerfectSpawnSettings.SettingEntry se = null;
        if (event.entityPlayer.worldObj.isRemote) {

            se = PerfectSpawnClientHandler.currentServerSettings;
        } else {

            se = PerfectSpawn.settings.getValidSettingEntry();
        }

        if (se != null && se.forceBed) {

            WorldProvider provider = event.entityPlayer.worldObj.provider;
            EntityPlayer player = event.entityPlayer;
            World worldObj = player.worldObj;

            if (provider.dimensionId == se.spawnDimension) {

                event.result = EntityPlayer.EnumStatus.OK;
                if (player.isPlayerSleeping() || !player.isEntityAlive()) {
                    event.result = EntityPlayer.EnumStatus.OTHER_PROBLEM;
                }

                if (player.worldObj.isDaytime() && provider.isSurfaceWorld()) {
                    event.result = EntityPlayer.EnumStatus.NOT_POSSIBLE_NOW;
                }

                if (!worldObj.isRemote
                    && (Math.abs(player.posX - event.x) > 3.0D || Math.abs(player.posY - event.y) > 2.0D
                        || Math.abs(player.posZ - event.z) > 3.0D)) {
                    event.result = EntityPlayer.EnumStatus.TOO_FAR_AWAY;
                }

                double d0 = 8.0D;
                double d1 = 5.0D;
                List list = player.worldObj.getEntitiesWithinAABB(
                    EntityMob.class,
                    AxisAlignedBB.getBoundingBox(
                        event.x - d0,
                        event.y - d1,
                        event.z - d0,
                        event.x + d0,
                        event.y + d1,
                        event.z + d0));

                if (!list.isEmpty()) {
                    event.result = EntityPlayer.EnumStatus.NOT_SAFE;
                }
            }

            if ((!worldObj.isRemote && event.result == EntityPlayer.EnumStatus.OK) || worldObj.isRemote) {

                if (player.isRiding()) {
                    player.mountEntity((Entity) null);
                }

                setSize((Entity) player, 0.2F, 0.2F);
                player.yOffset = 0.2F;

                if (player.worldObj.blockExists(event.x, event.y, event.z)) {

                    int l = worldObj.getBlock(event.x, event.y, event.z)
                        .getBedDirection((IBlockAccess) worldObj, event.x, event.y, event.z);
                    float f1 = 0.5F;
                    float f = 0.5F;

                    switch (l) {

                        case 0:
                            f = 0.9F;
                            break;
                        case 1:
                            f1 = 0.1F;
                            break;
                        case 2:
                            f = 0.1F;
                            break;
                        case 3:
                            f1 = 0.9F;
                            break;
                    }
                    func_71013_b(player, l);
                    player.setPosition((event.x + f1), (event.y + 0.9375F), (event.z + f));
                } else {

                    player.setPosition((event.x + 0.5F), (event.y + 0.9375F), (event.z + 0.5F));
                }

                try {
                    this.sleepTimer.set(player, Integer.valueOf(0));
                    this.sleeping.set(player, Boolean.valueOf(true));
                } catch (Exception e) {

                    PerfectSpawn.instance.logger.log(Level.ERROR, "Couldn't reflect on player bed data");
                    e.printStackTrace();
                }

                player.playerLocation = new ChunkCoordinates(event.x, event.y, event.z);
                player.motionX = player.motionZ = player.motionY = 0.0D;

                if (!player.worldObj.isRemote) {
                    player.worldObj.updateAllPlayersSleepingFlag();
                }
            }
        }
    }

    private void func_71013_b(EntityPlayer player, int par1) {
        player.field_71079_bU = 0.0F;
        player.field_71089_bV = 0.0F;

        switch (par1) {

            case 0:
                player.field_71089_bV = -1.8F;
                break;
            case 1:
                player.field_71079_bU = 1.8F;
                break;
            case 2:
                player.field_71089_bV = 1.8F;
                break;
            case 3:
                player.field_71079_bU = -1.8F;
                break;
        }
    }

    public static void setSpawnPoint(int dimension, int spawnX, int spawnY, int spawnZ) {
        WorldServer worldServer = DimensionManager.getWorld(dimension);
        if (worldServer instanceof WorldServerMulti) {

            WorldServerMulti w = (WorldServerMulti) DimensionManager.getWorld(dimension);
            DerivedWorldInfo worldInfo = (DerivedWorldInfo) w.getWorldInfo();

            try {
                Field f = DerivedWorldInfo.class.getDeclaredField(MCPNames.field("field_76115_a"));
                f.setAccessible(true);
                WorldInfo info = (WorldInfo) f.get(worldInfo);
                info.setSpawnPosition(spawnX, spawnY, spawnZ);
            } catch (Exception e) {

                PerfectSpawn.instance.logger.log(Level.ERROR, "Couldn't set spawn position");
                e.printStackTrace();
            }

        } else {

            WorldInfo info = worldServer.getWorldInfo();
            info.setSpawnPosition(spawnX, spawnY, spawnZ);
        }
    }

    private void setSize(Entity entity, float par1, float par2) {
        if (par1 != entity.width || par2 != entity.height) {

            float f = entity.width;
            entity.width = par1;
            entity.height = par2;
            entity.boundingBox.maxX = entity.boundingBox.minX + entity.width;
            entity.boundingBox.maxZ = entity.boundingBox.minZ + entity.width;
            entity.boundingBox.maxY = entity.boundingBox.minY + entity.height;

            if (entity.width > f && !entity.worldObj.isRemote) {
                entity.moveEntity((f - entity.width), 0.0D, (f - entity.width));
            }
        }

        float f2 = par1 % 2.0F;

        if (f2 < 0.375D) {

            entity.myEntitySize = Entity.EnumEntitySize.SIZE_1;
        } else if (f2 < 0.75D) {

            entity.myEntitySize = Entity.EnumEntitySize.SIZE_2;
        } else if (f2 < 1.0D) {

            entity.myEntitySize = Entity.EnumEntitySize.SIZE_3;
        } else if (f2 < 1.375D) {

            entity.myEntitySize = Entity.EnumEntitySize.SIZE_4;
        } else if (f2 < 1.75D) {

            entity.myEntitySize = Entity.EnumEntitySize.SIZE_5;
        } else {

            entity.myEntitySize = Entity.EnumEntitySize.SIZE_6;
        }
    }
}

/*
 * Location: /home/midnight/Downloads/PerfectSpawn-1.1-deobf.jar!/lumien/perfectspawn/Core/PSEventHandler.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version: 1.1.3
 */
