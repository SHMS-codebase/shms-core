package com.healthcaremngnt.projection;

import java.time.LocalDate;

public interface ActivePrescriptionProjection {
    Long getPrescriptionID();
    String getPatientName();
    String getDiagnosis();
    Long getPrescriptionDetailCount();
    LocalDate getPrescriptionDate();
    String getTreatmentStatus();
    Boolean getFollowupNeeded();
}