package com.healthcaremngnt.job.reader;

import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.healthcaremngnt.model.Patient;
import com.healthcaremngnt.repository.PatientRepository;

@Component
@StepScope
public class PatientReportItemReader implements ItemReader<Patient> {

	private static final Logger logger = LogManager.getLogger(PatientReportItemReader.class);

	@Autowired
	private PatientRepository patientRepository;

	private Iterator<Patient> patientIterator;

	@Value("#{jobParameters['admissionYear'] ?: '2020'}")
	private String admissionYear;

	@Value("#{jobParameters['reportType'] ?: 'daily'}")
	private String reportType;

	private boolean noPatientsAvailable = false;

	@Override
	public Patient read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		logger.info("PatientReportItemReader::: read()");
		if (patientIterator == null) {

			try {

				var patients = patientRepository.findPatientsByYear(Integer.parseInt(admissionYear));
				logger.debug("patients: {}", patients);
				logger.debug("Read {} patients total", patients.size());

				if (patients != null && !patients.isEmpty()) {
					patientIterator = patients.iterator();
				} else {
					noPatientsAvailable = true; // Set the flag
					patientIterator = List.<Patient>of().iterator();
					logger.warn("No patients found for the given date and report type.");
				}
			} catch (Exception e) {
				logger.error("Error in PatientItemReader: {}", e.getMessage());
				noPatientsAvailable = true; // Set the flag
				patientIterator = List.<Patient>of().iterator();
			}

		}

		return patientIterator.hasNext() ? patientIterator.next() : null;
	}

	public boolean isNoPatientsAvailable() {
		return noPatientsAvailable;
	}

}