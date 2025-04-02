package com.healthcaremngnt.job.writer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.healthcaremngnt.model.Appointment;

@Component
@StepScope
public class AppointmentRemainderPMItemWriter implements ItemWriter<Appointment> {

	private static final Logger logger = LogManager.getLogger(AppointmentRemainderPMItemWriter.class);

	@Override
	public void write(Chunk<? extends Appointment> chunk) throws Exception {

		logger.info("AppointmentRemainderPMItemWriter::: write() with {} items", chunk.getItems().size());
		// Send email to all patients in the appointment list!!

	}

}