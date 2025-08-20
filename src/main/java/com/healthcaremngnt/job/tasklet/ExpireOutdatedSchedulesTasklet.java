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

import com.healthcaremngnt.enums.ScheduleStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceContext;

@Component
@StepScope
public class ExpireOutdatedSchedulesTasklet implements Tasklet {

	private static final Logger logger = LogManager.getLogger(ExpireOutdatedSchedulesTasklet.class);

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		logger.info("ExpireOutdatedSchedulesTasklet:: execute - Start");

		EntityManager em = entityManagerFactory.createEntityManager();
		EntityTransaction transaction = em.getTransaction();

		try {
			transaction.begin();

			logger.info("Invoking expireOutdatedSchedules method");

//            String deleteQuery = "DELETE FROM DoctorSchedule ds WHERE ds.availableDate < CURRENT_DATE";
//            int deletedCount = em.createQuery(deleteQuery)
//                                .executeUpdate();

			String updateQuery = """
					UPDATE DoctorSchedule ds
					SET ds.scheduleStatus = :expiredStatus,
						ds.expiredDate = CURRENT_TIMESTAMP,
						ds.version = ds.version + 1
					WHERE ds.availableDate < CURRENT_DATE
						AND ds.scheduleStatus <> :expiredStatus
					""";
			int updatedCount = em.createQuery(updateQuery).setParameter("expiredStatus", ScheduleStatus.EXPIRED)
					.executeUpdate();

			if (updatedCount == 0) {
				logger.info("No outdated schedules found to mark as EXPIRED");
			} else {
				logger.info("Updated Schedule Status to 'EXPIRED' for {} outdated schedules", updatedCount);
			}

			transaction.commit();
			logger.info("ExpireOutdatedSchedulesTasklet:: execute - Success");

		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			logger.error("ExpireOutdatedSchedulesTasklet:: execute - Exception", e);
			throw e;
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}

		return RepeatStatus.FINISHED;
	}

}
