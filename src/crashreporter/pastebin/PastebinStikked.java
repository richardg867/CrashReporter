package crashreporter.pastebin;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.HttpUtil;

import crashreporter.api.PastebinProvider;
import crashreporter.api.PastebinProvider.PasteException;
import crashreporter.core.Http;

/**
 * Pastebin provider for Stikked-based pastebins
 * 
 * This remains unused since paste.minecraftforge.net is dead. 
 * 
 * @author Richard
 */
public class PastebinStikked implements PastebinProvider {
	private final String apiRoot;
	
	public PastebinStikked(String apiRoot) {
		this.apiRoot = apiRoot;
	}
	
	@Override
	public String paste(String title, String text) throws PasteException {
		Map<String, String> postvars = new HashMap<String, String>(4);
		postvars.put("text", text);
		postvars.put("title", title);
		postvars.put("name", "Crash Reporter");
		postvars.put("private", "1");
		
		try {
			return Http.post(new URL(apiRoot + "/create"), postvars).text.replaceAll("\n", "");
		} catch (Throwable e) {
			throw new PasteException(e);
		}
	}
}
