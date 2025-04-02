package com.healthcaremngnt.job.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.healthcaremngnt.model.Appointment;

@Component
public class AppointmentRemainderAMItemProcessor implements ItemProcessor<Appointment, Appointment> {

	private static final Logger logger = LogManager.getLogger(AppointmentRemainderAMItemProcessor.class);

	@Override
	public Appointment process(Appointment appointment) throws Exception {

		logger.info("AppointmentRemainderAMItemProcessor::: process()");
		// Need to send email remainder 
		
		return appointment;

	}

}