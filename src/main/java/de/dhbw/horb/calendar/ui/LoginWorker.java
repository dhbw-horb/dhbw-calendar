package de.dhbw.horb.calendar.ui;

import java.io.IOException;
import java.net.URL;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.StatusHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.dhbw.horb.calendar.dualis.DualisConnection;
import de.dhbw.horb.calendar.dualis.DualisException;
import de.dhbw.horb.calendar.dualis.DualisException.DualisAuthenticationException;

class LoginWorker implements Runnable {
	private final WebClient client;
	private HtmlPage page = null;
	private HtmlForm loginForm = null;
	private HtmlInput usernameField;
	private HtmlInput passwordField;
	private HtmlInput submitButton;
	private final String username;
	private final String password;
	private final LoginWorkerListener listener;

	public LoginWorker(final LoginWorkerListener listener, final String username, final String password) {
		this.listener = listener;
		this.username = username;
		this.password = password;

		client = new WebClient();
		client.setIncorrectnessListener(new IncorrectnessListener() {
			@Override
			public void notify(String message, Object origin) {
				System.out.println("=======");
				System.out.println(origin.getClass());
			}
		});
		// not used while setCssEnabled(false)
		client.setCssErrorHandler(new ErrorHandler() {
			@Override
			public void warning(CSSParseException exception)
					throws CSSException {
				System.out.println("CSS Exception: " + exception.getMessage());
			}

			@Override
			public void fatalError(CSSParseException exception)
					throws CSSException {
				System.out.println("CSS Exception: " + exception.getMessage());
			}

			@Override
			public void error(CSSParseException exception) throws CSSException {
				System.out.println("CSS Exception: " + exception.getMessage());
			}
		});
		client.setStatusHandler(new StatusHandler() {
			@Override
			public void statusMessageChanged(Page page, String message) {
				System.out
						.println("DualisConnection.getEvents().new StatusHandler() {...}.statusMessageChanged()");
			}
		});
		client.setHTMLParserListener(new HTMLParserListener() {
			@Override
			public void warning(String message, URL url, int line, int column,
					String key) {
				// ignore silently
			}

			@Override
			public void error(String message, URL url, int line, int column,
					String key) {
				// ignore silently
			}
		});
		client.setPrintContentOnFailingStatusCode(false);
		client.setJavaScriptEnabled(false);
		client.setCssEnabled(false);

	}

	private void step1() throws Exception {
		page = client.getPage(DualisConnection.DUALIS_URL);
	}

	private void step2() {
		loginForm = page.getFormByName("cn_loginForm");
		usernameField = loginForm.getInputByName("usrname");
		passwordField = loginForm.getInputByName("pass");
		submitButton = loginForm.getInputByValue("Anmelden");
	}

	private void step3() throws IOException {
		usernameField.type(username);
		passwordField.type(password);
		page = submitButton.click();
	}

	private void step4() throws DualisAuthenticationException {
		try {
			page.getAnchorByText("Stundenplan");
		} catch (ElementNotFoundException e) {
			throw new DualisException.DualisAuthenticationException(
					"Authentifizierung fehltgeschlagen", e);
		}
	}

	@Override
	public void run() {
		try {
			listener.setProgress(0.2, "Lade Seite");
			step1();

			listener.setProgress(0.4, "Analysiere Seite");
			step2();

			listener.setProgress(0.6, "Sende Login");
			step3();

			listener.setProgress(0.8, "Prüfe Login");
			step4();
			listener.setProgress(1.0, "Erfolgreich");

			listener.success(username,password);
		} catch (Exception e) {
			e.printStackTrace();
			listener.error(e);
		}
	}

	public interface LoginWorkerListener {
		public void setProgress(double d, String description);

		public void success(String username, String password);

		public void error(Exception e);
	}
}