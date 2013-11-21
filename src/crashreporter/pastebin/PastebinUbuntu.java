package crashreporter.pastebin;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.HttpUtil;

import crashreporter.api.PastebinProvider;
import crashreporter.core.Http;

/**
 * Pastebin provider for paste.ubuntu.com
 * 
 * You can blame MultiMC.
 * 
 * @author Richard
 */
public class PastebinUbuntu implements PastebinProvider {
	@Override
	public String paste(String title, String text) throws PasteException {
		Map<String, String> postvars = new HashMap<String, String>(3);
		postvars.put("syntax", "text");
		postvars.put("poster", "Crash Reporter");
		postvars.put("content", text);
		
		try {
			return Http.post(new URL("http://paste.ubuntu.com"), postvars).url;
		} catch (Throwable e) {
			throw new PasteException(e);
		}
	}
}
