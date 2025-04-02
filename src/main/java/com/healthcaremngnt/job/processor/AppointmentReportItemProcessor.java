package com.healthcaremngnt.job.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.healthcaremngnt.model.Appointment;

@Component
public class AppointmentReportItemProcessor implements ItemProcessor<Appointment, Appointment> {

	private static final Logger logger = LogManager.getLogger(AppointmentReportItemProcessor.class);

	@Override
	public Appointment process(Appointment appointment) throws Exception {

		logger.info("AppointmentReportItemProcessor::: process()");
		// Check for conditions for report generation ie, based on appointment date etc.

		return appointment;

	}

}