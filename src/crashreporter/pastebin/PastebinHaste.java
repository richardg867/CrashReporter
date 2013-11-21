package crashreporter.pastebin;

import java.net.URL;

import crashreporter.api.PastebinProvider;
import crashreporter.core.Http;

/**
 * Pastebin provider for hastebin.com
 * 
 * @author Richard
 */
public class PastebinHaste implements PastebinProvider {
	@Override
	public String paste(String title, String text) throws PasteException {
		try {
			String json = Http.post(new URL("http://hastebin.com/documents"), text).text;
			return "http://hastebin.com/" + json.substring(8, json.length() - 3) + ".hs";
		} catch (Throwable e) {
			throw new PasteException(e);
		}
	}
}
