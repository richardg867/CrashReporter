package crashreporter.api;

/**
 * Provides a notification provider.
 * 
 * Register notification providers using {@link Registry}.
 * 
 * Settings inside the <em>notify</em> configuration block are passed through {@link Configurable#parseConfig(String, String)}.
 * 
 * @author Richard
 */
public interface NotificationProvider extends Configurable {
	/**
	 * Send a notification.
	 * 
	 * @param title Title of the report paste, optional if unsupported
	 * @param url Report paste URL
	 * @throws NotifyException If the notification process failed
	 */
	public void notify(String title, String url) throws NotifyException;
	
	/**
	 * Notification process failed exception.
	 * 
	 * @author Richard
	 */
	public static class NotifyException extends RuntimeException {
		public NotifyException(String message) {
			super(message);
		}

		public NotifyException(Throwable cause) {
			super(cause);
		}
	}
}
