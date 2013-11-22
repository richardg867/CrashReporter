package crashreporter.core;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import cpw.mods.fml.common.FMLLog;

import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkListenThread;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;

/**
 * Log handler, the cleanest way of capturing crashes so far.
 * 
 * @author Richard
 */
public class ServerLogHandler extends Handler {
	public ServerLogHandler() {
		MinecraftServer.getServer().getLogAgent().func_120013_a().addHandler(this);
	}
	
	@Override
	public void publish(LogRecord record) {
		if (record.getMessage().startsWith("Failed to handle packet for ")) {
			CrashReporter.instance.report(new CrashReport("Exception while handling packet from " + record.getMessage().substring(28).split("/")[0], record.getThrown()));
		} else if (record.getMessage().startsWith("This crash report has been saved to: ")) {
			kickAllPlayers();
			
			String report;
			try {
				report = Util.readFileToString(new File(record.getMessage().substring(37)));
			} catch (Throwable e) {
				StringWriter writer = new StringWriter();
				writer.write("Crash report could not be read!\r\n\r\n");
				e.printStackTrace(new PrintWriter(writer));
				report = writer.toString();
			}
			
			CrashReporter.instance.report("Server crash", report);
		} else if (record.getMessage().equals("We were unable to save this crash report to disk.")) {
			kickAllPlayers();
			
			CrashReporter.instance.report("Server crash", "Crash report could not be saved!");
		}
	}

	@Override
	public void flush() {
		
	}

	@Override
	public void close() throws SecurityException {
		
	}
	
	private static void kickAllPlayers() {
		ServerConfigurationManager manager = MinecraftServer.getServer().getConfigurationManager();
		while (!manager.playerEntityList.isEmpty()) {
			((EntityPlayerMP) manager.playerEntityList.get(0)).playerNetServerHandler.kickPlayerFromServer("Server crashed");
		}
	}
}
