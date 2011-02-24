package de.dhbw.horb.calendar.dualis;

public abstract class DualisException extends Exception {
	public DualisException(String msg) {
		super(msg);
	}

	public DualisException(String msg, Throwable e) {
		super(msg, e);
	}

	private static final long serialVersionUID = 1L;

	public static class DualisAuthenticationException extends DualisException {
		private static final long serialVersionUID = 1L;

		public DualisAuthenticationException(String msg, Throwable e) {
			super(msg, e);
		}

		public DualisAuthenticationException(String msg) {
			super(msg);
		}
	}

	public static class DualisScrapingException extends DualisException {
		private static final long serialVersionUID = 1L;

		public DualisScrapingException(String msg, Throwable e) {
			super(msg, e);
		}
	}
}
