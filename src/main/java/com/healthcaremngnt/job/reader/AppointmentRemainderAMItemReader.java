package com.healthcaremngnt.job.reader;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.healthcaremngnt.model.Appointment;
import com.healthcaremngnt.repository.AppointmentRepository;

@Component
@StepScope
public class AppointmentRemainderAMItemReader implements ItemReader<Appointment> {

	private static final Logger logger = LogManager.getLogger(AppointmentRemainderAMItemReader.class);

	@Autowired
	private AppointmentRepository appointmentRepository;

	private Iterator<Appointment> appointmentIterator;

	private boolean noAppointmentsAvailable = false;

	@Override
	public Appointment read()
			throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		logger.info("AppointmentRemainderAMItemReader::: read()");
		if (appointmentIterator == null) {

			// logic to fetch appointment records for remainder
		}

		if (appointmentIterator.hasNext()) {
			return appointmentIterator.next();
		} else {
			return null; 
		}
	}

	public boolean isNoAppointmentsAvailable() {
		return noAppointmentsAvailable;
	}

}