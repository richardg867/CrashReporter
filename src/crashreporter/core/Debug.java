package crashreporter.core;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

/**
 * Debug command.
 * 
 * @author Richard
 */
public class Debug extends CommandBase {
	@Override
	public String getCommandName() {
		return "crash";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "/" + getCommandName();
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring) {
		icommandsender.getEntityWorld().activeChunkSet = null;
	}
}
