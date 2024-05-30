
package lumien.perfectspawn.Network;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import lumien.perfectspawn.Core.PerfectSpawnClientHandler;
import lumien.perfectspawn.Core.PerfectSpawnSettings;
import lumien.perfectspawn.PerfectSpawn;

public class PerfectSpawnSettingsMessage implements IMessage, IMessageHandler<PerfectSpawnSettingsMessage, IMessage> {

    PerfectSpawnSettings.SettingEntry se;
    boolean empty = true;

    public PerfectSpawnSettingsMessage(PerfectSpawnSettings.SettingEntry toSend) {
        this.se = toSend;
        this.empty = false;
    }

    public IMessage onMessage(PerfectSpawnSettingsMessage message, MessageContext ctx) {
        PerfectSpawnClientHandler.currentServerSettings = message.se;

        if (message.se != null) {
            PerfectSpawn.instance.logger.log(Level.INFO, "Received Perfect Spawn Settings from Server");
        }
        return null;
    }

    public void fromBytes(ByteBuf buf) {
        this.se = null;
        this.empty = buf.readBoolean();

        if (!this.empty) {

            this.se = new PerfectSpawnSettings.SettingEntry(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
            this.se.setForceBed(buf.readBoolean());
            this.se.setExactSpawn(buf.readBoolean());
        }
    }

    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.empty);

        if (!this.empty) {

            buf.writeInt(this.se.getSpawnDimension());

            buf.writeInt(this.se.getSpawnX());
            buf.writeInt(this.se.getSpawnY());
            buf.writeInt(this.se.getSpawnZ());

            buf.writeBoolean(this.se.forceBed());
            buf.writeBoolean(this.se.isExactSpawn());
        }
    }

    public PerfectSpawnSettingsMessage() {}
}

/*
 * Location:
 * /home/midnight/Downloads/PerfectSpawn-1.1-deobf.jar!/lumien/perfectspawn/Network/PerfectSpawnSettingsMessage.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version: 1.1.3
 */
