package de.dhbw.horb.calendar.ics;

import java.util.Calendar;
import java.util.TimeZone;

public class VEvent extends AbstractICSType {
	public Calendar dtstamp = Calendar.getInstance();
	public Calendar dtstart = Calendar.getInstance();
	public Calendar dtend = Calendar.getInstance();
	public String status = "";
	public String summary = "";
	public String description = "";
	public String location = "";

	/**
	 * {@inheritDoc} <br/>
	 * <br/>
	 * <b>See RFC section 3.6.1</b> for VEVENT Specification
	 */
	@Override
	public String serialize() {
		final StringBuffer sb = new StringBuffer();
		dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		sb.append("BEGIN:VEVENT\n");
		sb.append("DTSTART:" + dateTimeFormat.format(dtstart.getTime()) + "\n");
		sb.append("DTEND:" + dateTimeFormat.format(dtend.getTime()) + "\n");
		sb.append("DTSTAMP:" + dateTimeFormat.format(dtstamp.getTime()) + "\n");
		sb.append("UID:vevent_" + dateTimeFormat.format(dtstamp.getTime())
				+ "@dhbw.de\n");
		sb.append("CREATED:"
				+ dateTimeFormat.format(Calendar.getInstance(TimeZone
						.getTimeZone("UTC")).getTime()) + "\n");
		sb.append("STATUS:" + escapeText(status) + "\n");
		sb.append("SUMMARY:" + escapeText(summary) + "\n");
		sb.append("DESCRIPTION:" + escapeText(description) + "\n");
		sb.append("LOCATION:" + escapeText(location) + "\n");
		sb.append("TRANSP:OPAQUE\n");
		sb.append("END:VEVENT\n");
		return sb.toString();
	}

	public static class Builder {
		VEvent event = new VEvent();

		public Builder dtstamp(Calendar cstart) {
			event.dtstamp = cstart;
			return this;
		}

		public Builder dtstart(Calendar cstart) {
			event.dtstart = cstart;
			return this;
		}

		public Builder dtend(Calendar cend) {
			event.dtend = cend;
			return this;
		}

		public Builder summary(String summary) {
			event.summary = summary;
			return this;
		}

		public Builder description(String description) {
			event.description = description;
			return this;
		}

		public Builder location(String location) {
			event.location = location;
			return this;
		}

		public VEvent build() {
			return event;
		}

		public Builder status(String status) {
			event.status = status;
			return this;
		}
	}
}
