package de.dhbw.horb.calendar.ics;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent an iCalender Object.
 *
 * A iCalender object can consist of many components of different type. They are
 * implemented as subclasses of {@link AbstractICSType} and can be added to an
 * instance of {@link ICalendarObject}.
 */
public class ICalendarObject {
	List<AbstractICSType> icsObjects = new ArrayList<AbstractICSType>();
	private final String title;

	public ICalendarObject(final String title) {
		this.title = title;
	}

	public void add(AbstractICSType ics) {
		icsObjects.add(ics);
	}

	/**
	 *
	 * @return serialized iCalendar Object
	 */
	public String serialize() {
		final StringBuilder sb = new StringBuilder();
		// Einleiten des Kalenders
		sb.append("BEGIN:VCALENDAR\n");
		// This property specifies the identifier for the product that created
		// the iCalendar object.
		sb.append("PRODID:-//DHBW Horb i08005//DHBW Calender 0.1//EN\n");
		// Datenformat version
		sb.append("VERSION:2.0\n");
		// This property defines the calendar scale used for the
		// calendar information specified in the iCalendar object.
		sb.append("CALSCALE:GREGORIAN\n");
		// This property defines the iCalendar object method
		// associated with the calendar object.
		sb.append("METHOD:PUBLISH\n");
		/* Extensions */
		//
		sb.append("X-WR-TIMEZONE:UTC\n");
		// Name des Kalenders
		sb.append("X-WR-CALDESC:" + AbstractICSType.escapeText(title) + "\n");
		sb.append("X-WR-CALNAME:" + AbstractICSType.escapeText(title) + "\n");

		// Elemente
		for (AbstractICSType ics : icsObjects) {
			sb.append(ics.serialize());
		}
		sb.append("END:VCALENDAR\n");
		return sb.toString();
	}
}
