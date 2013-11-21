package crashreporter.api;

/**
 * Provides a pastebin service.
 * 
 * Register pastebin providers using {@link Registry}.
 * 
 * @author Richard
 */
public interface PastebinProvider {
	/**
	 * Paste to this pastebin.
	 * 
	 * @param title Title of the paste, optional if unsupported
	 * @param text Text to paste
	 * @return Pastebin entry URL or other identifier
	 * @throws PasteException If the pasting failed
	 */
	public String paste(String title, String text) throws PasteException;
	
	/**
	 * Paste failed exception.
	 * 
	 * @author Richard
	 */
	public static class PasteException extends RuntimeException {
		public PasteException(String message) {
			super(message);
		}

		public PasteException(Throwable cause) {
			super(cause);
		}
	}
}
