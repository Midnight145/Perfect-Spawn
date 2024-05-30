package lumien.perfectspawn.Network;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import lumien.perfectspawn.Core.PerfectSpawnClientHandler;
import lumien.perfectspawn.Core.PerfectSpawnSettings.SettingEntry;
import lumien.perfectspawn.PerfectSpawn;

public class PerfectSpawnSettingsMessage implements IMessage, IMessageHandler<PerfectSpawnSettingsMessage, IMessage> {

    SettingEntry se;
    boolean empty = true;

    public PerfectSpawnSettingsMessage() {

    }

    public PerfectSpawnSettingsMessage(SettingEntry toSend) {
        this.se = toSend;
        this.empty = false;
    }

    @Override
    public IMessage onMessage(PerfectSpawnSettingsMessage message, MessageContext ctx) {
        PerfectSpawnClientHandler.currentServerSettings = message.se;

        if (message.se != null) {
            PerfectSpawn.instance.logger.log(Level.INFO, "Received Perfect Spawn Settings from Server");
        }
        return null;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        se = null;
        empty = buf.readBoolean();

        if (!empty) {
            se = new SettingEntry(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
            se.setForceBed(buf.readBoolean());
            se.setExactSpawn(buf.readBoolean());
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(empty);

        if (!empty) {
            buf.writeInt(se.getSpawnDimension());

            buf.writeInt(se.getSpawnX());
            buf.writeInt(se.getSpawnY());
            buf.writeInt(se.getSpawnZ());

            buf.writeBoolean(se.forceBed());
            buf.writeBoolean(se.isExactSpawn());
        }
    }
}
