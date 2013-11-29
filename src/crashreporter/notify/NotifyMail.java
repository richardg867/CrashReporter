package crashreporter.notify;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.bouncycastle.util.encoders.Base64;

import crashreporter.api.NotificationProvider;
import crashreporter.core.Util;

/**
 * SMTP mail notification provider.
 * 
 * @author Richard
 */
public class NotifyMail implements NotificationProvider {
	private String server;
	private int port = 25;
	private boolean ssl = false;
	private String domain = "localhost.localdomain";
	private String username;
	private String password;
	private String from;
	private String[] to;
	private String subject = "{title}";
	private List<String> headers = new LinkedList<String>();
	
	@Override
	public void parseConfig(String key, String value) throws ConfigurationException {
		if (key.equals("server")) {
			server = value;
		} else if (key.equals("port")) {
			try {
				port = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				throw new ConfigurationException("Invalid port number: " + value);
			}
		} else if (key.equals("ssl")) {
			ssl = true;
		} else if (key.equals("domain")) {
			domain = value;
		} else if (key.equals("username")) {
			username = value;
		} else if (key.equals("password")) {
			password = value;
		} else if (key.equals("from")) {
			from = value;
		} else if (key.equals("to")) {
			to = value.split(",");
		} else if (key.equals("subject")) {
			subject = value;
		} else if (key.equals("header")) {
			headers.add(value);
		}
	}

	@Override
	public void notify(String title, String text, String url) throws NotifyException {
		Socket socket;
		
		try {
			if (ssl) {
				socket = SSLSocketFactory.getDefault().createSocket(server, port);
				((SSLSocket) socket).startHandshake();
			} else {
				socket = new Socket(server, port);
			}
			
			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String resp;
			
			// read the banner
			while (reader.readLine() == null);
			
			// I know it may be against spec to try and authenticate without a EHLO, but reading all the lines would be a pain
			writer.println("HELO " + domain);
			while ((resp = reader.readLine()) == null);
			if (!resp.startsWith("250 ")) {
				throw new IllegalStateException("Server responded to HELO with: " + resp);
			}
			
			if (username != null && password != null) {
				String auth = username + "\0" + username + "\0" + password;
				writer.println("AUTH PLAIN " + new String(Base64.encode(auth.getBytes())));
				while ((resp = reader.readLine()) == null);
				if (!resp.startsWith("235 ")) {
					throw new IllegalStateException("Server responded to AUTH with: " + resp);
				}
			}
			
			writer.println("MAIL FROM:<" + (from == null ? username : from) + ">");
			while ((resp = reader.readLine()) == null);
			if (!resp.startsWith("250 ")) {
				throw new IllegalStateException("Server responded to MAIL with: " + resp);
			}
			
			for (String recipient : to) {
				writer.println("RCPT TO:<" + recipient + ">");
				while ((resp = reader.readLine()) == null);
				if (!resp.startsWith("250 ")) {
					throw new IllegalStateException("Server responded to RCPT with: " + resp);
				}
			}
			
			writer.println("DATA");
			while ((resp = reader.readLine()) == null);
			if (!resp.startsWith("354 ")) {
				throw new IllegalStateException("Server responded to DATA with: " + resp);
			}
			
			writer.println("From: <" + (from == null ? username : from) + ">");
			writer.print("To: ");
			for (String recipient : to) {
				writer.print("<" + recipient + ">, ");
			}
			writer.println();
			writer.println("Subject: " + subject.replace("{title}", title));
			writer.println("X-Mailer: " + Util.getUserAgent());
			
			for (String header : headers) {
				writer.println(header);
			}
			
			writer.println();
			
			for (String line : text.split("\n")) {
				if (line.equals(".")) { // who knows if a crash report has a single dot line
					writer.println("..");
				} else {
					writer.println(line);
				}
			}
			
			writer.println(".");
			while ((resp = reader.readLine()) == null);
			if (!resp.startsWith("250 ")) {
				throw new IllegalStateException("Server responded to end of message with: " + resp);
			}
			
			writer.println("QUIT");
		} catch (Throwable e) {
			e.printStackTrace();
			throw new NotifyException(e);
		}
	}
}
