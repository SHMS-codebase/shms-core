package com.healthcaremngnt.service.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcaremngnt.model.Specialization;
import com.healthcaremngnt.repository.SpecializationRepository;
import com.healthcaremngnt.service.SpecializationService;

@Service
@Transactional
public class SpecializationServiceImpl implements SpecializationService {

	private static final Logger logger = LogManager.getLogger(SpecializationServiceImpl.class);

	private final SpecializationRepository specializationRepository;

	public SpecializationServiceImpl(SpecializationRepository specializationRepository) {
		this.specializationRepository = specializationRepository;
	}

	@Override
	public List<Specialization> getAllSpecializations() {

		logger.info("Retrieving all Specializations from DB");
		
		return specializationRepository.findAll();
	}

}