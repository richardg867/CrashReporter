package crashreporter.pastebin;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import crashreporter.api.PastebinProvider;
import crashreporter.api.PastebinProvider.PasteException;
import crashreporter.core.Http;

/**
 * Pastebin provider for sprunge.us
 * 
 * @author Richard
 */
public class PastebinSprunge implements PastebinProvider {
	@Override
	public String paste(String title, String text) throws PasteException {
		Map<String, String> postvars = new HashMap<String, String>(1);
		postvars.put("sprunge", text);
		
		try {
			return Http.post(new URL("http://sprunge.us"), postvars).text.trim().replaceAll("\n", "");
		} catch (Throwable e) {
			throw new PasteException(e);
		}
	}
}
