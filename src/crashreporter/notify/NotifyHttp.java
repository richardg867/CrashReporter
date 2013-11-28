package crashreporter.notify;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.util.encoders.Base64;

import crashreporter.api.NotificationProvider;
import crashreporter.core.Http;

/**
 * Generic HTTP notification provider.
 *
 * @author Richard
 */
public class NotifyHttp implements NotificationProvider {
	private String url;
	private String post;
	private String auth;
	
	@Override
	public void parseConfig(String key, String value) throws ConfigurationException {
		if (key.equals("url")) {
			url = value;
		} else if (key.equals("post")) {
			post = value;
		} else if (key.equals("authenticate")) {
			// thank 493 for bouncycastle having a base64 encoder, can't touch sun.* or Commons
			auth = new String(Base64.encode(value.getBytes()));
		}
	}

	@Override
	public void notify(String title, String url) throws NotifyException {
		Map<String, String> headers = null;
		if (auth != null) {
			headers = new HashMap<String, String>(1);
			headers.put("Authorization", "Basic " + auth);
		}
		
		URL destUrl;
		try {
			destUrl = new URL(replaceKeywords(this.url, title, url));
		} catch (MalformedURLException e) {
			return;
		}
		
		try {
			Http.post(destUrl, replaceKeywords(post, title, url), headers);
		} catch (Throwable e) {
			throw new NotifyException(e);
		}
	}
	
	private String replaceKeywords(String in, String title, String url) {
		try {
			return in.replace("{title}", URLEncoder.encode(title, "UTF-8")).replace("{link}", URLEncoder.encode(url, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			return in;
		}
	}
}
