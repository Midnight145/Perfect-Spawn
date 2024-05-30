
package lumien.perfectspawn.Core;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import lumien.perfectspawn.PerfectSpawn;

public class PerfectSpawnCommand extends CommandBase {

    public String getCommandName() {
        return "ps";
    }

    public List addTabCompletionOptions(ICommandSender cs, String[] strings) {
        if (strings.length == 1) {
            return getListOfStringsMatchingLastWord(strings, new String[] { "reload" });
        }
        return null;
    }

    public String getCommandUsage(ICommandSender var1) {
        return "/ps reload";
    }

    public void processCommand(ICommandSender commandUser, String[] arguments) {
        if (arguments.length == 0) {
            return;
        }

        String subCommand = arguments[0];
        if (subCommand.equals("reload")) {

            commandUser.addChatMessage((IChatComponent) new ChatComponentText("Reloading PerfectSpawn settings"));
            PerfectSpawn.settings.reload();
        }
    }
}

/*
 * Location: /home/midnight/Downloads/PerfectSpawn-1.1-deobf.jar!/lumien/perfectspawn/Core/PerfectSpawnCommand.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version: 1.1.3
 */
