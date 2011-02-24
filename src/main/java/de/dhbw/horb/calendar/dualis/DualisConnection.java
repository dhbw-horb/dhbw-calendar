package de.dhbw.horb.calendar.dualis;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomAttr;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.dhbw.horb.calendar.dualis.DualisException.DualisAuthenticationException;
import de.dhbw.horb.calendar.dualis.DualisException.DualisScrapingException;
import de.dhbw.horb.calendar.ics.VEvent;

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

	@SuppressWarnings("deprecation")
	public List<VEvent> getEvents() throws FailingHttpStatusCodeException,
			IOException, DualisScrapingException, DualisAuthenticationException {
		HtmlPage page, loginPage;
		final HtmlForm loginForm;
		final HtmlInput usernameField, passwordField, submitButton;
		final WebClient webClient = new WebClient();
		final List<VEvent> events = new ArrayList<VEvent>();

		webClient.setJavaScriptEnabled(false);
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
					"Link Studenplan 'Woche' nicht gefunden", e);
		}
		page = anchorMonat.click();

		for (HtmlElement elem : page.getElementsByTagName("div")) {
			if (!elem.getAttribute("class").equals("tbMonthDay"))
				continue;
			final Date day;
			Object dayTitle = elem
					.getFirstByXPath("div[@class='tbsubhead']/a/@title");
			if (dayTitle instanceof DomAttr) {
				String value = ((DomAttr) dayTitle).getValue();
				try {
					day = dateFormat.parse(value);
				} catch (ParseException e) {
					e.printStackTrace();
					continue;
				}
			} else {
				System.err.println("kann datum nicht bestimmen");
				continue;
			}
			for (HtmlElement appLink : elem.getElementsByTagName("a")) {
				if (!appLink.getAttribute("class").equals("apmntLink"))
					continue;
				// title="08:15 - 10:45 / HOR-121 / Open Source Systeme"
				String desc = StringUtils.strip(appLink.getAttribute("title"));
				java.util.regex.Matcher matcher = appLinkPattern.matcher(desc);
				if (matcher.matches() && matcher.groupCount() == 4) {
					String timeStart = matcher.group(1);
					String timeEnd = matcher.group(2);
					String room = matcher.group(3);
					String title = matcher.group(4);
					System.out.println(timeStart + "-" + timeEnd + "-" + room
							+ "-" + title + "-" + day);
					int hStart = Integer.parseInt(timeStart.split(":")[0]);
					int mStart = Integer.parseInt(timeStart.split(":")[1]);
					int hEnd = Integer.parseInt(timeEnd.split(":")[0]);
					int mEnd = Integer.parseInt(timeEnd.split(":")[1]);
					Date start = (Date) day.clone();
					start.setHours(hStart);
					start.setMinutes(mStart);
					Date end = (Date) day.clone();
					end.setHours(hEnd);
					end.setMinutes(mEnd);
					VEvent event = new VEvent.Builder().dtstart(start)
							.dtstamp(start).dtend(end).summary(title).description(title)
							.location(room).status("CONFIRMED").build();
					events.add(event);
				} else {
					System.err.println("Skip: " + desc + " at day " + day);
				}
			}
		}

		return events;
	}

	// Parse Dates
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"dd.MM.yyyy");
	
	// Appointment Link Title
	// example: 08:15 - 10:45 / HOR-121 / Open Source Systeme
	private static final Pattern appLinkPattern = Pattern
			.compile("(\\d{2}:\\d{2})\\s-\\s(\\d{2}:\\d{2})\\s\\/\\s(.+)\\s\\/\\s(.+)");

}