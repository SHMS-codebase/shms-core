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

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceContext;

@Component
@StepScope
public class ArchivePatientRecordsTasklet implements Tasklet {

	private static final Logger logger = LogManager.getLogger(ArchivePatientRecordsTasklet.class);

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		logger.info("ArchivePatientRecordsTasklet:: execute - Start");

		EntityManager em = entityManagerFactory.createEntityManager();
		EntityTransaction transaction = em.getTransaction();

		try {
			transaction.begin();

			// Should move the patient records to archive tables when:
			// ● Patient Profile remains inactive for 1 year
			// (ie, no appointments, treatments & zero pending bills)
			String updateQuery = "";
			int updateCount = em.createQuery(updateQuery).executeUpdate();

			transaction.commit();
			logger.info("Archived {} patient records", updateCount);
			logger.info("ArchivePatientRecordsTasklet:: execute - Success");
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			logger.error("ArchivePatientRecordsTasklet:: execute - Exception", e);
			throw e;
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}

		return RepeatStatus.FINISHED;
	}

}
