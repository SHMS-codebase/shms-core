package com.healthcaremngnt.job.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.healthcaremngnt.model.Patient;

@Component
public class PatientReportItemProcessor implements ItemProcessor<Patient, Patient> {

	private static final Logger logger = LogManager.getLogger(PatientReportItemProcessor.class);

	@Override
	public Patient process(Patient patient) throws Exception {

		logger.info("PatientReportItemProcessor::: process()");
		// Check for conditions for report generation ie, based on Patient date etc.

		return patient;

	}

}