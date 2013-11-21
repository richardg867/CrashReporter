package crashreporter.api;

/**
 * Helper interface for configuration file parsing.
 * 
 * @author Richard
 */
public interface Configurable {
	/**
	 * Parse a key/value configuration pair.
	 * 
	 * @param key Key
	 * @param value Value
	 * @throws ConfigurationException If an invalid value was given
	 */
	public void parseConfig(String key, String value) throws ConfigurationException;
	
	public static class ConfigurationException extends RuntimeException {
		public ConfigurationException(String message) {
			super(message);
		}

		public ConfigurationException(Throwable cause) {
			super(cause);
		}
	}
}
