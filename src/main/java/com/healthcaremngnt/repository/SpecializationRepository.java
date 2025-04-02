package com.healthcaremngnt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.healthcaremngnt.model.Specialization;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, Integer> {

	Specialization findBySpecializationID(int specializationID);

	// If findBySpecializationID is just an alias for findById, remove it and use the inherited findById
	// Optional<Specialization> findBySpecializationID(int specializationID); <- Remove if redundant, use findById

	// Example of a derived query (if needed):
	// Optional<Specialization> findBySpecializationNameIgnoreCase(String specializationName); // Case-insensitive search

}