package com.healthcaremngnt.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.healthcaremngnt.model.Prescription;

@Repository
public interface PrescriptionRespository extends JpaRepository<Prescription, Long> {

	Optional<Prescription> findByTreatment_TreatmentID(Long treatmentID);

}