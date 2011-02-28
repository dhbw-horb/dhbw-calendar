package de.dhbw.horb.calendar.ics;

import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringEscapeUtils;

public abstract class AbstractICSType {
	/**
	 * Serialisierert dieses Calenderteil. Dem Ergebnis Fehlt noch der vCalendar
	 * Header und Footer
	 *
	 */
	public abstract String serialize();

	/**
	 * <b>RFC says:</b>
	 * <pre>
	 * FORM #2: DATE WITH UTC TIME
	 *
	 *       The date with UTC time, or absolute time, is identified by a LATIN
	 *       CAPITAL LETTER Z suffix character, the UTC designator, appended to
	 *       the time value.  For example, the following represents January 19,
	 *       1998, at 0700 UTC:
	 *
	 *        19980119T070000Z
	 *
	 *       The "TZID" property parameter MUST NOT be applied to DATE-TIME
	 *       properties whose time values are specified in UTC.
	 * </pre>
	 */
	public static SimpleDateFormat dateTimeFormat = new SimpleDateFormat(
			"yyyyMMdd'T'HHmmss'Z'");

	/**
	 * described in <b>RFC section 3.3.11</b>
	 *
	 * @param text
	 * @return
	 */
	public static String escapeText(String text) {
		return StringEscapeUtils.escapeJava(text); //XXX TODO
	}
}
