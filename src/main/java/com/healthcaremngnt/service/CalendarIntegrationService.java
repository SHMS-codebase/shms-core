package com.healthcaremngnt.service;

import java.io.IOException;

import com.google.api.client.util.DateTime;

public interface CalendarIntegrationService {

	String createAppointmentEvent(DateTime googleStart, DateTime googleEnd, String summary) throws IOException;

	void cancelAppointmentEvent(String eventId) throws IOException;

}
