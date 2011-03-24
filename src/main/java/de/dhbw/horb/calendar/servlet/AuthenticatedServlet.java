package de.dhbw.horb.calendar.servlet;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ws.commons.util.Base64;

public abstract class AuthenticatedServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private String loginRealm = "Servlet Login";

	public void setLoginRealm(String loginRealm) {
		this.loginRealm = loginRealm;
	}

	public String getLoginRealm() {
		return loginRealm;
	}

	String[] getUsernamePassword(HttpServletRequest request) throws IOException {
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null) {
			StringTokenizer st = new StringTokenizer(authHeader);
			if (st.hasMoreTokens()) {
				String basic = st.nextToken();
				if (basic.equalsIgnoreCase("Basic")) {
					String credentials = st.nextToken();

					String userPass = new String(Base64.decode(credentials));

					// The decoded string is in the form
					// "userID:password".
					int p = userPass.indexOf(":");
					if (p != -1) {
						String username = userPass.substring(0, p).trim();
						String password = userPass.substring(p + 1).trim();
						String[] userPassword = new String[] { username,
								password };
						if ("".equals(userPassword) || "".equals(password)) {
							return null;
						} else {
							return new String[] { username, password };
						}
					}
				}
			}
		}
		return null;
	}

	void writeAuthenticationRequest(HttpServletResponse response) {
		String realm = StringEscapeUtils.escapeJava(getLoginRealm());
		String s = "Basic realm=\"" + realm + "\"";
		response.setHeader("WWW-Authenticate", s);
		response.setStatus(401);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String[] usernamePassword = getUsernamePassword(request);
		if (usernamePassword == null) {
			writeAuthenticationRequest(response);
			return;
		}
		doGet(request, response, usernamePassword[0], usernamePassword[1]);
	}

	protected abstract void doGet(HttpServletRequest request,
			HttpServletResponse response, String username, String password) throws ServletException, IOException;
}
