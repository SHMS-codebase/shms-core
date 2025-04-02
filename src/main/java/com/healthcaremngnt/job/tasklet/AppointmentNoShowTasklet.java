package com.healthcaremngnt.job.tasklet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.healthcaremngnt.enums.AppointmentStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceContext;

@Component
@StepScope
public class AppointmentNoShowTasklet implements Tasklet {

	private static final Logger logger = LogManager.getLogger(AppointmentNoShowTasklet.class);

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		logger.info("AppointmentNoShowTasklet:: execute - Start");

		EntityManager em = entityManagerFactory.createEntityManager();
		EntityTransaction transaction = em.getTransaction();

		try {
			transaction.begin();

			// Get current hour to determine if it's the 9 PM run
			int currentHour = java.time.LocalTime.now().getHour();

			String updateQuery;

			if (currentHour == 21) { // 9 PM logic
				logger.info("Executing 9 PM logic: Updating all remaining scheduled records for the day.");

				updateQuery = "UPDATE Appointments a SET a.appointmentStatus = '" + AppointmentStatus.NOSHOW + "' "
						+ "WHERE a.appointmentStatus = '" + AppointmentStatus.SCHEDULED + "' "
						+ "AND a.AppointmentDate = CURRENT_DATE";

			} else { // Regular logic (12 PM to 8 PM)
				logger.info("Executing regular logic: Updating appointments scheduled 2+ hours ago.");

				updateQuery = "UPDATE Appointments a SET a.appointmentStatus = '" + AppointmentStatus.NOSHOW + "' "
						+ "WHERE a.appointmentStatus = '" + AppointmentStatus.SCHEDULED + "' "
						+ "AND a.AppointmentDate = CURRENT_DATE "
						+ "AND a.AppointmentTime < CURRENT_TIME - INTERVAL 2 HOUR";
			}

			int updateCount = em.createQuery(updateQuery).executeUpdate();

			transaction.commit();
			logger.info("Updated {} No Show Appointments!!", updateCount);
			logger.info("AppointmentNoShowTasklet:: execute - Success");
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			logger.error("AppointmentNoShowTasklet:: execute - Exception", e);
			throw e;
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}

		return RepeatStatus.FINISHED;
	}

}
