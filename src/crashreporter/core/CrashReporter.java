package crashreporter.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.crash.CrashReport;
import net.minecraft.server.MinecraftServer;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.relauncher.Side;
import crashreporter.api.NotificationProvider;
import crashreporter.api.NotificationProvider.NotifyException;
import crashreporter.api.PastebinProvider;
import crashreporter.api.Registry;
import crashreporter.api.PastebinProvider.PasteException;
import crashreporter.pastebin.*;
import crashreporter.notify.*;

@Mod(modid = "CrashReporter", name = "Crash Reporter", version = CrashReporter.VERSION)
public class CrashReporter {
	public static final String VERSION = "@VERSION@";
	public static CrashReporter instance;
	
	public Logger log;
	public Configuration config;
	public PastebinProvider pastebin;
	public List<NotificationProvider> notificationProviders = new LinkedList<NotificationProvider>();
	
	@EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		instance = this;
		log = event.getModLog();
		
		new APIHandler();

		Registry.registerPastebinProvider("pastebin", new PastebinCom());
		Registry.registerPastebinProvider("hastebin", new PastebinHaste());
		Registry.registerPastebinProvider("sprunge", new PastebinSprunge());
		Registry.registerPastebinProvider("ubuntu", new PastebinUbuntu());
		
		Registry.registerNotificationProvider("forgeirc", NotifyForgeIRC.class);
		Registry.registerNotificationProvider("http", NotifyHttp.class);
		Registry.registerNotificationProvider("mail", NotifyMail.class);
	}
	
	@EventHandler
	public void onInit(FMLInitializationEvent event) {
		config = new Configuration();
	}
	
	@EventHandler
	public void onPostInit(FMLPostInitializationEvent event) {
		
	}
	
	@EventHandler
	public void onServerStarting(FMLServerStartingEvent event) {
		// act as a paperweight on client, since the logger getter on ILogAgent is server-only
		if (FMLCommonHandler.instance().getSide() == Side.SERVER) new ServerLogHandler();
		
		event.registerServerCommand(new Debug());
	}
	
	public void report(CrashReport report) {
		report(report.getDescription(), report.getCompleteReport());
	}
	
	public void report(String title, String text) {
		String link = null;		
		for (PastebinProvider provider : Registry.getPastebinProviders()) {
			try {
				link = provider.paste(title, text);
				break;
			} catch (PasteException e) {
				e.printStackTrace(); // FIXME
			}
		}
		
		if (link == null) {
			log.log(Level.SEVERE, "No pastebin providers could handle the request");
			link = "<No link>";
		}
		
		log.log(Level.INFO, "Report posted to: " + link);
		
		for (NotificationProvider provider : notificationProviders) {
			try {
				provider.notify(title, text, link);
			} catch (NotifyException e) {
				e.printStackTrace(); // FIXME
			}
		}
	}
}
