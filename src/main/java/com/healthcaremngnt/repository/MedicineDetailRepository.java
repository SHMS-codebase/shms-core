package com.healthcaremngnt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.healthcaremngnt.model.MedicineDetail;

public interface MedicineDetailRepository extends JpaRepository<MedicineDetail, Long> {

}