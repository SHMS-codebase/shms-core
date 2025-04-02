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
public class DeleteExpiredSchedulesTasklet implements Tasklet {

    private static final Logger logger = LogManager.getLogger(DeleteExpiredSchedulesTasklet.class);
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("DeleteExpiredSchedulesTasklet:: execute - Start");
        
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        
        try {
            transaction.begin();
            
            logger.info("Invoking deleteExpiredSchedules method");
            String deleteQuery = "DELETE FROM DoctorSchedule ds WHERE ds.availableDate < CURRENT_DATE";
            int deletedCount = em.createQuery(deleteQuery)
                                .executeUpdate();
            
            transaction.commit();
            logger.info("Deleted {} expired schedules", deletedCount);
            logger.info("DeleteExpiredSchedulesTasklet:: execute - Success");
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("DeleteExpiredSchedulesTasklet:: execute - Exception", e);
            throw e;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        
        return RepeatStatus.FINISHED;
    }
    
}
