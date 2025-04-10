package com.healthcaremngnt.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.healthcaremngnt.enums.InvoiceStatus;
import com.healthcaremngnt.model.Invoice;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

	List<Invoice> findByInvoiceDate(LocalDate queryDate);

	@Query("SELECT i FROM Invoice i WHERE FUNCTION('DATE', i.invoiceDate) BETWEEN :startDate AND :endDate")
	List<Invoice> findInvoicesByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	@Query("SELECT i FROM Invoice i JOIN Treatment t ON i.treatment.treatmentID = t.treatmentID "
			+ "WHERE (:patientID IS NULL OR t.patientID = :patientID) "
			+ "AND (:invoiceID IS NULL OR i.invoiceID = :invoiceID) "
			+ "AND (:invoiceDate IS NULL OR FUNCTION('DATE', i.invoiceDate) = :invoiceDate) "
			+ "AND (:invoiceStatus IS NULL OR i.invoiceStatus = :invoiceStatus)")
	List<Invoice> findInvoices(@Param("patientID") Long patientID, @Param("invoiceID") Long invoiceID,
			@Param("invoiceDate") LocalDate invoiceDate, @Param("invoiceStatus") InvoiceStatus invoiceStatus);

}