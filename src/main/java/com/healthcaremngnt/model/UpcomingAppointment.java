package com.healthcaremngnt.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpcomingAppointment(Long appointmentID, Long patientID, String patientName, String doctorName,
		String specialization, LocalDate appointmentDate, LocalTime appointmentTime, String appointmentStatus,
		String remainingTime, boolean eligibleForCancellation) {
}