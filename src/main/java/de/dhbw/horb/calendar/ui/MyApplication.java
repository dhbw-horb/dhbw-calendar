package de.dhbw.horb.calendar.ui;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.LoginForm.LoginListener;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Window;

import de.dhbw.horb.calendar.ui.InteractiveLoginWorker.LoginWorkerListener;

public class MyApplication extends Application {
	private static final long serialVersionUID = 1L;

	@Override
	public void init() {
		setMainWindow(new MyMainWindow());
	}

	public static class MyMainWindow extends Window {
		private static final long serialVersionUID = 1L;
		private TabSheet tabSheet = new TabSheet();
		private LoginComponent loginComponent;
		private StundenplanComponent stundenplanComponent;
		private LogoutComponent logoutComponent;
		private String password = "";
		private String username = "";
		{
			{
				loginComponent = new LoginComponent(this);
				stundenplanComponent = new StundenplanComponent(this);
				logoutComponent = new LogoutComponent(this);
			}
		}

		public MyMainWindow() {
			super("DHBW Calendar");
			tabSheet.addTab(loginComponent, "Login", null);
			addComponent(tabSheet);
		}

		public void login(String username, String password) {
			this.username = username;
			this.password = password;
			tabSheet.removeTab(tabSheet.getTab(loginComponent));

			tabSheet.addTab(stundenplanComponent, "Stundenplan", null);
			tabSheet.addTab(logoutComponent, "Logout", null);
		}

		public void logout() {
			this.username = this.password = "";
			tabSheet.removeTab(tabSheet.getTab(stundenplanComponent));
			tabSheet.removeTab(tabSheet.getTab(logoutComponent));

			tabSheet.addTab(loginComponent, "Login", null);
		}

		public String getUsername() {
			return username;
		}

		public String getPassword() {
			return password;
		}
	}

	private static class LogoutComponent extends FormLayout {
		private static final long serialVersionUID = 1L;

		private final MyMainWindow mainWindow;

		public LogoutComponent(MyMainWindow mainWindow) {
			this.mainWindow = mainWindow;
			Button logoutButton = new Button("Logout");
			logoutButton.addListener(new ClickListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					LogoutComponent.this.mainWindow.logout();
				}
			});
			addComponent(logoutButton);
		}
	}

	private static class LoginComponent extends FormLayout {
		private static final long serialVersionUID = 1L;

		private final MyMainWindow mainWindow;
		private final DualisLoginForm loginForm;
		private final Label labelDescription;
		private final ProgressIndicator progressIndicator;
		private final LoginWorkerListener loginWorkerListener;
		{
			{
				loginForm = new DualisLoginForm();
				labelDescription = new Label("");
				
				progressIndicator = new ProgressIndicator();
				progressIndicator.setVisible(false);
				progressIndicator.setSizeFull();
				
				loginWorkerListener = new LoginWorkerListener() {

					@Override
					public void success(String username, String password) {
						mainWindow.login(username, password);
						labelDescription.setValue("");
						progressIndicator.setVisible(false);
						loginForm.setVisible(true);
					}

					@Override
					public void setProgress(double d, String description) {
						progressIndicator.setValue(new Float(d));
						labelDescription.setValue(description);
					}

					@Override
					public void error(Exception e) {
						progressIndicator.setVisible(false);
						labelDescription.setValue("Fehler: " + e.getMessage());
						loginForm.setVisible(true);
					}
				};
			}
		}

		public LoginComponent(final MyMainWindow mainWindow) {
			this.mainWindow = mainWindow;
			
			loginForm.addListener(new LoginListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void onLogin(LoginEvent event) {
					new Thread(new InteractiveLoginWorker(loginWorkerListener, event
							.getLoginParameter("username"), event
							.getLoginParameter("password"))).start();
					loginForm.setVisible(false);
					progressIndicator.setVisible(true);
				}
			});
			addComponent(loginForm);
			addComponent(labelDescription);
			addComponent(progressIndicator);
		}
	}

	private static class DualisLoginForm extends LoginForm {
		private static final long serialVersionUID = 1L;
	}
}