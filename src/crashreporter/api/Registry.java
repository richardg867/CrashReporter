package crashreporter.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for API provider objects.
 * 
 * @author Richard
 */
public class Registry {
	private static final Map<String, PastebinProvider> pastebinProviders = new HashMap<String, PastebinProvider>();
	private static final Map<String, Class<? extends NotificationProvider>> notificationProviders = new HashMap<String, Class<? extends NotificationProvider>>();
	
	/**
	 * Register a {@link PastebinProvider}.
	 * 
	 * @param id ID name for the provider, used in the config file
	 * @param provider The provider
	 */
	public static void registerPastebinProvider(String id, PastebinProvider provider) {
		if (pastebinProviders.containsKey(id)) throw new IllegalArgumentException("Pastebin provider " + id + " already registered by " + pastebinProviders.get(id) + " when registering " + provider);
		
		pastebinProviders.put(id, provider);
	}
	
	/**
	 * Get a {@link PastebinProvider} by its ID.
	 * 
	 * @param id ID name for the provider
	 * @return The provider, or null if there is no such provider
	 */
	public static PastebinProvider getPastebinProvider(String id) {
		return pastebinProviders.get(id);
	}
	
	/**
	 * Get a list of {@link PastebinProvider}s, the first one being the user's preferred pastebin.
	 * 
	 * @return List of providers
	 */
	public static List<PastebinProvider> getPastebinProviders() {
		List<PastebinProvider> providers = new ArrayList<PastebinProvider>(pastebinProviders.size());
		
		// first the preferred one
		PastebinProvider preferred = CallHandler.instance.getPastebin();
		if (preferred != null) providers.add(preferred);
		// then the rest
		providers.addAll(pastebinProviders.values());
		
		return providers;
	}
	
	/**
	 * Get a map of all {@link PastebinProvider}s, in no particular order.
	 * 
	 * @return Map of providers
	 */
	public static Map<String, PastebinProvider> getAllPastebinProviders() {
		return Collections.unmodifiableMap(pastebinProviders);
	}
	
	/**
	 * Register a {@link NotificationProvider} class.
	 * 
	 * @param id ID name for the provider, used in the config file
	 * @param provider The provider class
	 */
	public static void registerNotificationProvider(String id, Class<? extends NotificationProvider> provider) {
		if (notificationProviders.containsKey(id)) throw new IllegalArgumentException("Notification provider " + id + " already registered by " + notificationProviders.get(id) + " when registering " + provider);
		
		notificationProviders.put(id, provider);
	}
	
	/**
	 * Get a {@link NotificationProvider} class by its ID.
	 * 
	 * @param id ID name for the provider class
	 * @return The provider class, or null if there is no such provider
	 */
	public static Class<? extends NotificationProvider> getNotificationProvider(String id) {
		return notificationProviders.get(id);
	}
	
	/**
	 * Get a list of {@link NotificationProvider} classes.
	 * 
	 * @return List of provider classes
	 * @see CallHandler#getActiveNotificationProviders()
	 */
	public static List<Class<? extends NotificationProvider>> getNotificationProviders() {
		List<Class<? extends NotificationProvider>> providers = new ArrayList<Class<? extends NotificationProvider>>(notificationProviders.size());
		providers.addAll(notificationProviders.values());
		
		return providers;
	}
	
	/**
	 * Get a map of all {@link NotificationProvider} classes.
	 * 
	 * @return Map of provider classes
	 * @see CallHandler#getActiveNotificationProviders()
	 */
	public static Map<String, Class<? extends NotificationProvider>> getAllNotificationProviders() {
		return Collections.unmodifiableMap(notificationProviders);
	}
}
