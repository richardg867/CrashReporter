package crashreporter.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import crashreporter.api.Configurable;
import crashreporter.api.Configurable.ConfigurationException;
import crashreporter.api.NotificationProvider;
import crashreporter.api.PastebinProvider;
import crashreporter.api.Registry;

/**
 * Configuration handler class.
 * 
 * @author Richard
 */
public class Configuration {
	private File f;
	private Configurable currentConfigurable;
	
	public Configuration() {
		f = new File("config", "crashreporter.cfg");
		
		if (!f.exists()) {
			try {
				InputStream example = Configuration.class.getResourceAsStream("/crashreporter/example.cfg");
				OutputStream out = new FileOutputStream(f);
				Util.copyStream(example, out);
				out.flush();
				out.close();
				example.close();
			} catch (Throwable e) {
				CrashReporter.instance.log.log(Level.WARNING, "Failed to extract example config file!", e);
			}
		}
		
		try {
			read();
		} catch (Throwable e) {
			CrashReporter.instance.log.log(Level.WARNING, "Failed to read config file!", e);
		}
	}
	
	private void read() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(f));
		
		int lineno = 0;
		String line;
		while ((line = reader.readLine()) != null) {
			lineno++;
			if (line.isEmpty() || line.charAt(0) == '#') continue;
			
			int idx = line.indexOf(' ');
			
			String key, value;
			if (idx == -1) {
				key = line;
				value = "";
			} else {
				key = line.substring(0, idx);
				value = line.substring(idx + 1);
			}
			
			try {
				parseConfig(key, value);
			} catch (ConfigurationException e) {
				reader.close(); // SHUT UP ECLIPSE
				throw new ConfigurationException(f.getName() + ":" + lineno + ": " + e.getMessage());
			}
		}
		
		reader.close();
	}
	
	private void parseConfig(String key, String value) throws ConfigurationException {
		if (currentConfigurable != null) {
			if (key.equals("end")) currentConfigurable = null;
			else currentConfigurable.parseConfig(key, value);
			return;
		}
		
		if (key.equals("pastebin")) {
			PastebinProvider provider = Registry.getPastebinProvider(value);
			if (provider == null) {
				throw new ConfigurationException("Invalid pastebin provider: " + value);
			} else {
				CrashReporter.instance.pastebin = provider;
			}
		} else if (key.equals("notify")) {
			Class<? extends NotificationProvider> provider = Registry.getNotificationProvider(value);
			if (provider == null) {
				throw new ConfigurationException("Invalid notification provider: " + value);
			} else {
				NotificationProvider providerInstance;
				try {
					providerInstance = provider.newInstance();
				} catch (Throwable e) {
					throw new ConfigurationException("Failed to create notification provider: " + value);
				}
				
				CrashReporter.instance.notificationProviders.add(providerInstance);
				currentConfigurable = providerInstance;
			}
		} else if (key.equals("untrusted-ssl")) {
			// http://stackoverflow.com/questions/1828775/how-to-handle-invalid-ssl-certificates-with-apache-httpclient
			try {
				SSLContext ctx = SSLContext.getInstance("TLS");
				ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
				SSLContext.setDefault(ctx);
			} catch (Throwable e) {
				CrashReporter.instance.log.log(Level.WARNING, "Failed to enable untrusted SSL", e);
			}
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
