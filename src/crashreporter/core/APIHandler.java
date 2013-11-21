package crashreporter.core;

import java.util.Collections;
import java.util.List;

import crashreporter.api.CallHandler;
import crashreporter.api.NotificationProvider;
import crashreporter.api.PastebinProvider;

public class APIHandler extends CallHandler {
	public APIHandler() {
		instance = this;
	}
	
	@Override
	public PastebinProvider getPastebin() {
		return CrashReporter.instance.pastebin;
	}

	@Override
	public List<NotificationProvider> getActiveNotificationProviders() {
		return Collections.unmodifiableList(CrashReporter.instance.notificationProviders);
	}
}
