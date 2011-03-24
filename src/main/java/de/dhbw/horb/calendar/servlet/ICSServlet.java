package de.dhbw.horb.calendar.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dhbw.horb.calendar.dualis.DualisConnection;
import de.dhbw.horb.calendar.dualis.DualisException;
import de.dhbw.horb.calendar.ics.ICalendarObject;
import de.dhbw.horb.calendar.ics.VEventComponent;

/**
 * Das ICS-Servlet ist ein HTTP-Servlet das von der Kalenderapplikation
 * aufgerufen wird um die iCalendar Daten zu bekommen.
 */
public class ICSServlet extends AuthenticatedServlet {
	private static final long serialVersionUID = 1L;

	public ICSServlet() {
		setLoginRealm("Dualis Calendar Connector");
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response, String username, String password)
			throws IOException {
		response.setHeader("Content-Type", "text/calendar; charset=utf-8");

		ICalendarObject calendar = new ICalendarObject("DHBW Calendar");
		try {
			DualisConnection connection = new DualisConnection(username,
					password);
			for (VEventComponent event : connection.getEvents()) {
				calendar.add(event);
			}
		} catch (DualisException e) {
			e.printStackTrace();
		}
		response.getOutputStream().write(calendar.serialize().getBytes());
	}
}