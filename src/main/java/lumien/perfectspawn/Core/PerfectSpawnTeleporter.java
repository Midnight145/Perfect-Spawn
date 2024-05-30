
package lumien.perfectspawn.Core;

import net.minecraft.entity.Entity;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class PerfectSpawnTeleporter extends Teleporter {

    public PerfectSpawnTeleporter(WorldServer worldServer) {
        super(worldServer);
    }

    public void placeInPortal(Entity par1Entity, double par2, double par4, double par6, float par8) {}

    public boolean placeInExistingPortal(Entity par1Entity, double par2, double par4, double par6, float par8) {
        return false;
    }

    public boolean makePortal(Entity par1Entity) {
        return false;
    }

    public void removeStalePortalLocations(long par1) {}
}

/*
 * Location: /home/midnight/Downloads/PerfectSpawn-1.1-deobf.jar!/lumien/perfectspawn/Core/PerfectSpawnTeleporter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version: 1.1.3
 */
