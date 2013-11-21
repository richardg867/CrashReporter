package crashreporter.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

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
			if (idx == -1) continue;
			
			String key = line.substring(0, idx);
			String value = line.substring(idx + 1);
			
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
		}
	}
}
