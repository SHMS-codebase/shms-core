package com.healthcaremngnt.service.impl;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.healthcaremngnt.service.CalendarIntegrationService;

@Service
public class CalendarIntegrationServiceImpl implements CalendarIntegrationService {
	
	private static final Logger logger = LogManager.getLogger(CalendarIntegrationServiceImpl.class);


	@Autowired
	private Calendar calendarService;

	@Override
	public String createAppointmentEvent(DateTime start, DateTime end, String summary) throws IOException {
		
		logger.info("Creating Appointment Event in Google Calendar!!!");
		
		Event event = new Event().setSummary(summary)
				.setStart(new EventDateTime().setDateTime(new DateTime(start.toString())).setTimeZone("Asia/Kolkata"))
				.setEnd(new EventDateTime().setDateTime(new DateTime(end.toString())).setTimeZone("Asia/Kolkata"));

		Event createdEvent = calendarService.events().insert("primary", event).execute();
		logger.info("Google Calendar event link: {}", createdEvent.getHtmlLink());
		
		listUpcomingEvents(); // List upcoming events for verification
		
		return createdEvent.getId(); // Store this for cancellation
	}

	@Override
	public void cancelAppointmentEvent(String eventId) throws IOException {
		calendarService.events().delete("primary", eventId).execute();
	}
	
	public void listUpcomingEvents() throws IOException {
	    Events events = calendarService.events().list("primary")
	        .setMaxResults(10)
	        .setOrderBy("startTime")
	        .setSingleEvents(true)
	        .setTimeMin(new DateTime(System.currentTimeMillis()))
	        .execute();

	    for (Event event : events.getItems()) {
	        logger.info("Event: {} at {}", event.getSummary(), event.getStart().getDateTime());
	    }
	}

}
