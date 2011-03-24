package de.dhbw.horb.calendar.ui;

import java.util.Date;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import de.dhbw.horb.calendar.dualis.DualisConnection;
import de.dhbw.horb.calendar.ics.VEventComponent;
import de.dhbw.horb.calendar.ui.MyApplication.MyMainWindow;

public class StundenplanComponent extends FormLayout {
	private static final long serialVersionUID = 1L;

	private ClickListener buttonFetchListener = new ClickListener() {
		private static final long serialVersionUID = 1L;

		@Override
		public void buttonClick(ClickEvent event) {
			table.removeAllItems();
			table.setEnabled(false);
			buttonFetch.setVisible(false);
			StundenplanComponent.this.addComponentAsFirst(refresher);

			new Thread() {
				public void run() {
					DualisConnection dualisConnection = new DualisConnection(
							myMainWindow.getUsername(),
							myMainWindow.getPassword());
					try {
						for (VEventComponent vevent : dualisConnection.getEvents()) {
							synchronized (getApplication()) {
								table.addItem(new Object[] { vevent.dtstart.getTime(),
										vevent.dtend.getTime(), vevent.summary,
										vevent.location },
										vevent.dtstart.getTime());
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						table.addItem(new Object[] { new Date(), new Date(),
								"Fehler", e.getMessage() });
					}
					synchronized (getApplication()) {
						buttonFetch.setVisible(true);
						table.setEnabled(true);
						StundenplanComponent.this.removeComponent(refresher);
					}
				};
			}.start();
		}
	};

	private final Button buttonFetch = new Button("Kalender holen",
			buttonFetchListener);

	/**
	 * The Progress Indicator have no real sense. Its used to force the client
	 * to poll all 500ms for new events
	 */
	final ProgressIndicator refresher = new ProgressIndicator();
	{
		{
			refresher.setPollingInterval(500);
			refresher.setSizeFull();
		}
	}

	private final Table table = new Table("Stundenplan");
	{
		{
			table.setSizeFull();
			table.setSelectable(true);
			table.addContainerProperty("begin", Date.class, "");
			table.addContainerProperty("end", Date.class, "");
			table.addContainerProperty("summary", String.class, "");
			table.addContainerProperty("location", String.class, "");

		}
	}

	private final MyMainWindow myMainWindow;

	public StundenplanComponent(MyMainWindow myMainWindow) {
		this.myMainWindow = myMainWindow;

		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(buttonFetch);
		layout.addComponent(table);
		layout.setSizeFull();

		addComponent(layout);
		layout.setComponentAlignment(buttonFetch, Alignment.MIDDLE_CENTER);
	}
}