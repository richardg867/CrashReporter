package crashreporter.notify;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.util.encoders.Base64;

import crashreporter.api.NotificationProvider;
import crashreporter.core.CrashReporter;
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
			
			// HACK: Java has no way of allowing self signed certificates without tampering with global context...
			// http://stackoverflow.com/questions/1828775/how-to-handle-invalid-ssl-certificates-with-apache-httpclient
			if (url.startsWith("https://")) {
				try {
					SSLContext ctx = SSLContext.getInstance("TLS");
					ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
					SSLContext.setDefault(ctx);
				} catch (Throwable e) {}
			}
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
	
	private static class DefaultTrustManager implements X509TrustManager {
		@Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}
}
