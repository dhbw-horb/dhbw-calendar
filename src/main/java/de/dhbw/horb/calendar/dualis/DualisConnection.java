package de.dhbw.horb.calendar.dualis;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.StatusHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomAttr;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.dhbw.horb.calendar.dualis.DualisException.DualisAuthenticationException;
import de.dhbw.horb.calendar.dualis.DualisException.DualisScrapingException;
import de.dhbw.horb.calendar.ics.VEventComponent;

public class DualisConnection {
	public static final String DUALIS_URL = "https://dualis.dhbw.de/scripts/mgrqcgi?APPNAME=CampusNet&PRGNAME=EXTERNALPAGES&ARGUMENTS=-N000000000000001,-N,-Awelcome";

	private final String username;
	private final String password;

	/**
	 * Baut eine Verbindung mit Dualis auf und führt einen Login durch
	 *
	 * @param username
	 *            Dualis Benutzername
	 * @param password
	 *            Dualis Password
	 * @throws FailingHttpStatusCodeException
	 * @throws IOException
	 * @throws DualisScrapingException
	 * @throws DualisAuthenticationException
	 */
	public DualisConnection(final String username, final String password) {
		this.username = username;
		this.password = password;
	}

	public List<VEventComponent> getEvents() throws FailingHttpStatusCodeException,
			IOException, DualisScrapingException, DualisAuthenticationException {
		HtmlPage page, loginPage;
		final HtmlForm loginForm;
		final HtmlInput usernameField, passwordField, submitButton;
		final WebClient webClient = new WebClient();
		final List<VEventComponent> events = new ArrayList<VEventComponent>();

		webClient.setIncorrectnessListener(new IncorrectnessListener() {
			@Override
			public void notify(String message, Object origin) {
				System.out.println("=======");
				System.out.println(origin.getClass());
			}
		});
		// not used while setCssEnabled(false)
		webClient.setCssErrorHandler(new ErrorHandler() {
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
		webClient.setStatusHandler(new StatusHandler() {
			@Override
			public void statusMessageChanged(Page page, String message) {
				System.out
						.println("DualisConnection.getEvents().new StatusHandler() {...}.statusMessageChanged()");
			}
		});
		webClient.setHTMLParserListener(new HTMLParserListener() {
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
		webClient.setPrintContentOnFailingStatusCode(false);
		webClient.setJavaScriptEnabled(false);
		webClient.setCssEnabled(false);
		loginPage = webClient.getPage(DUALIS_URL);
		try {
			loginForm = loginPage.getFormByName("cn_loginForm");
		} catch (ElementNotFoundException e) {
			throw new DualisException.DualisScrapingException(
					"Fehler beim Suchen des Login Formular auf der Startseite mit Name 'cn_loginForm'",
					e);
		}
		try {
			usernameField = loginForm.getInputByName("usrname");
		} catch (ElementNotFoundException e) {
			throw new DualisException.DualisScrapingException(
					"Fehler beim Suchen des Username Input mit Name 'usrname'",
					e);
		}
		usernameField.type(username);
		try {
			passwordField = loginForm.getInputByName("pass");
		} catch (ElementNotFoundException e) {
			throw new DualisException.DualisScrapingException(
					"Fehler beim Suchen des Password Input mit Name 'pass'", e);
		}
		passwordField.type(password);

		try {
			submitButton = loginForm.getInputByValue("Anmelden");
		} catch (ElementNotFoundException e) {
			throw new DualisException.DualisScrapingException(
					"Fehler beim Suchen des Login Submit Buttons mit Text 'Anmelden'",
					e);
		}
		page = submitButton.click();
		HtmlAnchor anchorByText;
		try {
			anchorByText = page.getAnchorByText("Stundenplan");
		} catch (ElementNotFoundException e) { // Login failed
			throw new DualisException.DualisAuthenticationException(
					"Authentifizierung fehltgeschlagen", e);
		}
		page = anchorByText.click();

		HtmlAnchor anchorMonat;
		try {
			anchorMonat = page.getAnchorByText("Monat");
		} catch (ElementNotFoundException e) {
			throw new DualisException.DualisScrapingException(
					"Link Studenplan 'Monat' nicht gefunden", e);
		}
		page = anchorMonat.click();

		getEventsFromPage(page, events);

		HtmlAnchor anchorNextMonth;
		try {
			anchorNextMonth = page.getAnchorByName("skipForward_btn");
		} catch (ElementNotFoundException e) {
			throw new DualisException.DualisScrapingException(
					"Link Studenplan 'Nächster Monat' nicht gefunden", e);
		}
		page = anchorNextMonth.click();

		getEventsFromPage(page, events);

		return events;
	}

	private void getEventsFromPage(HtmlPage page, final List<VEventComponent> events) {
		for (HtmlElement elem : page.getElementsByTagName("div")) {
			if (!elem.getAttribute("class").equals("tbMonthDay")) {
				continue;
			}
			final Calendar cday = Calendar.getInstance();
			Object dayTitle = elem
					.getFirstByXPath("div[@class='tbsubhead']/a/@title");
			if (dayTitle instanceof DomAttr) {
				String value = ((DomAttr) dayTitle).getValue();
				try {
					cday.setTime(dateFormat.parse(value));
				} catch (ParseException e) {
					e.printStackTrace();
					continue;
				}
			} else {
				System.err.println("kann datum nicht bestimmen");
				continue;
			}
			for (HtmlElement appLink : elem.getElementsByTagName("a")) {
				if (!appLink.getAttribute("class").equals("apmntLink")) {
					continue;
				}
				// title="08:15 - 10:45 / HOR-121 / Open Source Systeme"
				String desc = StringUtils.strip(appLink.getAttribute("title"));
				java.util.regex.Matcher matcher = appLinkPattern.matcher(desc);
				if (matcher.matches() && matcher.groupCount() == 4) {
					String timeStart = matcher.group(1);
					String timeEnd = matcher.group(2);
					String room = matcher.group(3);
					String title = matcher.group(4);
					int hStart = Integer.parseInt(timeStart.split(":")[0]);
					int mStart = Integer.parseInt(timeStart.split(":")[1]);
					int hEnd = Integer.parseInt(timeEnd.split(":")[0]);
					int mEnd = Integer.parseInt(timeEnd.split(":")[1]);
					Calendar cstart = (Calendar) cday.clone();
					cstart.set(Calendar.HOUR_OF_DAY, hStart);
					cstart.set(Calendar.MINUTE, mStart);

					Calendar cend = (Calendar) cday.clone();
					cend.set(Calendar.HOUR_OF_DAY, hEnd);
					cend.set(Calendar.MINUTE, mEnd);

					cstart.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
					cend.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
					VEventComponent event = new VEventComponent.Builder().dtstart(cstart)
							.dtstamp(cstart).dtend(cend).summary(title)
							.description(title).location(room)
							.status("CONFIRMED").build();
					events.add(event);
				} else {
					System.err.println("Skip: " + desc + " at day "
							+ cday.getTime());
				}
			}
		}
	}

	// Parse Dates
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"dd.MM.yyyy");

	// Appointment Link Title
	// example: 08:15 - 10:45 / HOR-121 / Open Source Systeme
	private static final Pattern appLinkPattern = Pattern
			.compile("(\\d{2}:\\d{2})\\s-\\s(\\d{2}:\\d{2})\\s\\/\\s(.+)\\s\\/\\s(.+)");

}