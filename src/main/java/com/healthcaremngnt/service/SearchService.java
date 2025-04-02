package com.healthcaremngnt.service;

import java.time.LocalDate;
import java.util.List;

import com.healthcaremngnt.enums.AppointmentStatus;
import com.healthcaremngnt.enums.InvoiceStatus;
import com.healthcaremngnt.enums.ScheduleStatus;
import com.healthcaremngnt.enums.TreatmentStatus;
import com.healthcaremngnt.model.Appointment;
import com.healthcaremngnt.model.DoctorSchedule;
import com.healthcaremngnt.model.Invoice;
import com.healthcaremngnt.model.SearchResult;
import com.healthcaremngnt.model.Treatment;

public interface SearchService {

	List<SearchResult> searchUsers(String roleName, String name, String emailID, String contactNumber,
			String specializationName);

	List<DoctorSchedule> searchSchedules(Long doctorID, LocalDate availableDate, String specialization,
			ScheduleStatus scheduleStatus);

	List<Appointment> searchAppointments(Long patientID, Long doctorID, LocalDate appointmentDate,
			AppointmentStatus appointmentStatus);

	List<Invoice> searchInvoices(Long patientID, Long invoiceID, LocalDate invoiceDate, InvoiceStatus invoiceStatus);

	List<Treatment> searchTreatments(Long patientID, Long treatmentID, LocalDate treatmentDate,
			TreatmentStatus treatmentStatus);

}