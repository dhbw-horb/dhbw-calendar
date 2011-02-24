package de.dhbw.horb.calendar.ics;

import java.util.Date;

public class VEvent extends AbstractICSType {
	public Date dtstamp = new Date();
	public Date dtstart = new Date();
	public Date dtend = new Date();
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
		sb.append("BEGIN:VEVENT\n");
		sb.append("DTSTART:" + dateTimeFormat.format(dtstart) + "\n");
		sb.append("DTEND:" + dateTimeFormat.format(dtend) + "\n");
		sb.append("DTSTAMP:" + dateTimeFormat.format(dtstamp) + "\n");
		sb.append("UID:vevent_" + dateTimeFormat.format(dtstamp) + "@dhbw.de\n");
		sb.append("CREATED:" + dateTimeFormat.format(new Date()) + "\n");
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

		public Builder dtstamp(Date dtstamp) {
			event.dtstamp = dtstamp;
			return this;
		}

		public Builder dtstart(Date dtstart) {
			event.dtstart = dtstart;
			return this;
		}

		public Builder dtend(Date dtend) {
			event.dtend = dtend;
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
