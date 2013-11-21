package crashreporter.notify;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;

import crashreporter.api.NotificationProvider;
import crashreporter.core.CrashReporter;

/**
 * ForgeIRC notification provider.
 * 
 * @author Richard
 */
public class NotifyForgeIRC implements NotificationProvider {
	private boolean reflected = false;
	private Class ForgeIRC;
	private Field ForgeIRC_mBot;
	private Class Config;
	private Field Config_cName;
	private Class IRCLib;
	private Method IRCLib_sendMessage; 
	
	public NotifyForgeIRC() {
		try {
			try {
				ForgeIRC = Class.forName("com.forgeirc.ForgeIRC");
			} catch (ClassNotFoundException e) {
				// ForgeIRC is not installed
				return;
			}
			
			ForgeIRC_mBot = ForgeIRC.getDeclaredField("mBot");
			ForgeIRC_mBot.setAccessible(true);
			
			Config = Class.forName("com.forgeirc.Config");
			Config_cName = Config.getDeclaredField("cName");
			Config_cName.setAccessible(true);
			
			IRCLib = Class.forName("irclib.IRCLib");
			IRCLib_sendMessage = IRCLib.getDeclaredMethod("sendMessage", String.class, String.class);
			IRCLib_sendMessage.setAccessible(true);
			
			reflected = true;
		} catch (Throwable e) {
			e.printStackTrace();
			CrashReporter.instance.log.log(Level.WARNING, "Could not reflect into ForgeIRC!", e);
		}
	}
	
	@Override
	public void parseConfig(String key, String value) throws ConfigurationException {
		
	}

	@Override
	public void notify(String title, String url) throws NotifyException {
		if (!reflected) return;
		
		try {
			IRCLib_sendMessage.invoke(ForgeIRC_mBot.get(null), Config_cName.get(null), title + ": " + url);
		} catch (Throwable e) {
			CrashReporter.instance.log.log(Level.WARNING, "Could not reflect into ForgeIRC!", e);
		}
	}
}
