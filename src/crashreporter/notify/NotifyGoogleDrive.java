package crashreporter.notify;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import crashreporter.api.NotificationProvider;
import crashreporter.core.Http;

/**
 * Google Forms notification provider.
 * 
 * @author Richard
 */
public class NotifyGoogleDrive implements NotificationProvider {
	private static final Pattern FORM_PATTERN = Pattern.compile("<form action=\"([^\"]+)\" ");
	private static final Pattern INPUT_PATTERN = Pattern.compile("<input type=\"text\" name=\"([^\"]+)\" ");
	private static final Pattern TEXTAREA_PATTERN = Pattern.compile("<textarea name=\"([^\"]+)\" ");
	
	private String url;
	
	@Override
	public void parseConfig(String key, String value) throws ConfigurationException {
		if (key.equals("url")) {
			url = value;
		}
	}

	@Override
	public void notify(String title, String text, String url) throws NotifyException {
		if (url == null) throw new NotifyException("Form URL not set");
		
		String form;
		try {
			form = Http.post(new URL(this.url), null).text;
		} catch (Throwable e) {
			throw new NotifyException(e);
		}
		
		Matcher matcher;
		
		matcher = FORM_PATTERN.matcher(form);
		matcher.find();
		String formTarget = matcher.group(1);
		if (formTarget == null) throw new NotifyException("Could not find the main form");
		
		matcher = INPUT_PATTERN.matcher(form);
		matcher.find();
		String titleInput = matcher.group(1);
		if (titleInput == null) throw new NotifyException("Could not find a text question on the form");
		
		matcher = TEXTAREA_PATTERN.matcher(form);
		matcher.find();
		String textInput = matcher.group(1);
		if (textInput == null) throw new NotifyException("Could not find a paragraph text question on the form");
		
		Map<String, String> postvars = new HashMap<String, String>(2);
		postvars.put(titleInput, title);
		postvars.put(textInput, text);System.out.println(postvars);
		
		try {
			System.out.println(Http.post(new URL(formTarget), postvars).text);
		} catch (Throwable e) {
			throw new NotifyException(e);
		}
	}
}
