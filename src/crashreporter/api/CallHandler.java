package crashreporter.api;

import java.util.List;

/**
 * Generic API bridge.
 * 
 * @author Richard
 */
public abstract class CallHandler {
	/**
	 * Instance to the CallHandler implementation. Don't touch this.
	 */
	public static CallHandler instance;
	
	/**
	 * Get the preferred pastebin provider.
	 * 
	 * @return The preferred pastebin provider as chosen on the configuration file
	 */
	public abstract PastebinProvider getPastebin();
	
	/**
	 * Get the list of active notification providers.
	 * 
	 * @return The list of configured and active notification providers
	 */
	public abstract List<NotificationProvider> getActiveNotificationProviders();
}
