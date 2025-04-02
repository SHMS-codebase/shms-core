package com.healthcaremngnt.service.impl;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcaremngnt.exceptions.EmailAlreadyExistsException;
import com.healthcaremngnt.exceptions.UsernameAlreadyExistsException;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.Patient;
import com.healthcaremngnt.model.Role;
import com.healthcaremngnt.model.User;
import com.healthcaremngnt.model.UserDetails;
import com.healthcaremngnt.repository.DoctorRepository;
import com.healthcaremngnt.repository.PatientRepository;
import com.healthcaremngnt.repository.RoleRepository;
import com.healthcaremngnt.repository.UserRepository;
import com.healthcaremngnt.service.UserService;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final DoctorRepository doctorRepository;
	private final PatientRepository patientRepository;
	private final BCryptPasswordEncoder encoder;

	public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
			DoctorRepository doctorRepository, PatientRepository patientRepository) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.doctorRepository = doctorRepository;
		this.patientRepository = patientRepository;
		this.encoder = new BCryptPasswordEncoder(12);
	}

	@Override
	public User register(User user) throws UsernameAlreadyExistsException, EmailAlreadyExistsException {

		logger.info("Registering user: {}", user);

		if (userRepository.findByUserName(user.getUserName()).isPresent()) {
			throw new UsernameAlreadyExistsException("Username already exists");
		}

		user.setPassword(encoder.encode(user.getPassword()));
		logger.debug("Encoded Generated Password::: {}", user.getPassword());

		try {
			return userRepository.save(user);
		} catch (DataIntegrityViolationException e) {
			if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
				throw new EmailAlreadyExistsException("Email ID already exists");
			}
			throw new RuntimeException("An unexpected error occurred while registering the user", e);
		}
	}

	@Override
	public Optional<User> findByEmailID(String email) {

		logger.info("Retrieving user by email ID: {}", email);

		return userRepository.findByEmailID(email);
	}

	@Override
	public Optional<UserDetails> findUserDetailsByID(Long userID) {

		logger.info("Retrieving user details by user ID: {}", userID);

		return userRepository.findById(userID).map(user -> {
			UserDetails userDetails = new UserDetails();
			userDetails.setUserID(user.getUserID());
			userDetails.setRoleID(user.getRole().getRoleID());
			userDetails.setUserName(user.getUserName());
			userDetails.setEmailID(user.getEmailID());

			Optional<Role> role = roleRepository.findById(user.getRole().getRoleID());
			role.ifPresent(r -> {
				switch (r.getRoleName().toUpperCase()) {
				case "DOCTOR":
					Optional<Doctor> doctorOptional = doctorRepository.findByUser(user);
					if (doctorOptional.isPresent())
						userDetails.setDoctor(doctorOptional.get());
					break;
				case "PATIENT":
					Optional<Patient> patientOptional = patientRepository.findByUser(user);
					if (patientOptional.isPresent())
						userDetails.setPatient(patientOptional.get());
					break;
				}
			});
			logger.debug("User Details::: {}", userDetails);
			return userDetails;
		});
	}

	@Override
	public void updateUserDetails(UserDetails userDetails) {
		logger.info("Updating user details: {}", userDetails);

		try {
			validateUserDetails(userDetails);

			User user = userRepository.findById(userDetails.getUserID())
					.orElseThrow(() -> new IllegalArgumentException("User not found: " + userDetails.getUserID()));

			user.setUserName(userDetails.getUserName());
			user.setEmailID(userDetails.getEmailID());

			userRepository.save(user);

			if (userDetails.getDoctor() != null) {
				Doctor existingDoctor = doctorRepository.findById(userDetails.getDoctor().getDoctorID())
						.orElseThrow(() -> new IllegalArgumentException(
								"Doctor not found: " + userDetails.getDoctor().getDoctorID()));

				existingDoctor.setDoctorName(userDetails.getDoctor().getDoctorName());
				existingDoctor.setContactNumber(userDetails.getDoctor().getContactNumber());
				existingDoctor.setAddress(userDetails.getDoctor().getAddress());
				existingDoctor.setQualification(userDetails.getDoctor().getQualification());
				existingDoctor.setSpecialization(userDetails.getDoctor().getSpecialization());
				existingDoctor.setExperience(userDetails.getDoctor().getExperience());

				doctorRepository.save(existingDoctor);
			}

			if (userDetails.getPatient() != null) {
				Patient existingPatient = patientRepository.findById(userDetails.getPatient().getPatientID())
						.orElseThrow(() -> new IllegalArgumentException(
								"Patient not found: " + userDetails.getPatient().getPatientID()));

				existingPatient.setPatientName(userDetails.getPatient().getPatientName());
				existingPatient.setDob(userDetails.getPatient().getDob());
				existingPatient.setGender(userDetails.getPatient().getGender());
				existingPatient.setContactNumber(userDetails.getPatient().getContactNumber());
				existingPatient.setAddress(userDetails.getPatient().getAddress());

				patientRepository.save(existingPatient);
			}
		} catch (IllegalArgumentException e) {
			logger.error("Validation failed::: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred::: {}", e.getMessage());
			throw new RuntimeException("Failed to update user details", e);
		}
	}

	private void validateUserDetails(UserDetails userDetails) {
		if (userDetails.getUserName() == null || userDetails.getUserName().trim().isEmpty()) {
			throw new IllegalArgumentException("User name is required.");
		}
		if (userDetails.getUserName().length() < 3 || userDetails.getUserName().length() > 50) {
			throw new IllegalArgumentException("User name must be between 3 and 50 characters.");
		}
		if (!userDetails.getUserName().matches("^[a-zA-Z0-9 ]+$")) {
			throw new IllegalArgumentException("User name can only contain alphanumeric characters and spaces.");
		}
		if (userDetails.getEmailID() == null || userDetails.getEmailID().trim().isEmpty()) {
			throw new IllegalArgumentException("Email ID is required.");
		}
		if (!userDetails.getEmailID().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
			throw new IllegalArgumentException("Email ID must be a valid email address.");
		}
		if (userDetails.getDoctor() != null) {
			validateDoctorDetails(userDetails.getDoctor());
		}
		if (userDetails.getPatient() != null) {
			validatePatientDetails(userDetails.getPatient());
		}
	}

	private void validateDoctorDetails(Doctor doctor) {
		if (doctor.getDoctorName() == null || doctor.getDoctorName().trim().isEmpty()) {
			throw new IllegalArgumentException("Doctor name is required.");
		}
		if (doctor.getDoctorName().length() < 3 || doctor.getDoctorName().length() > 50) {
			throw new IllegalArgumentException("Doctor name must be between 3 and 50 characters.");
		}
		if (!doctor.getDoctorName().matches("^Dr\\.\\s?[a-zA-Z0-9 ]+$")) {
		    throw new IllegalArgumentException("Doctor name must start with 'Dr.' and can contain alphanumeric characters and spaces thereafter.");
		}
		if (doctor.getContactNumber() == null || doctor.getContactNumber().trim().isEmpty()) {
			throw new IllegalArgumentException("Contact number is required.");
		}
		if (!doctor.getContactNumber().matches("^[0-9]{10}$")) {
			throw new IllegalArgumentException("Contact number must be a valid 10-digit number.");
		}
		if (doctor.getQualification() == null || doctor.getQualification().trim().isEmpty()) {
			throw new IllegalArgumentException("Qualification is required.");
		}
		if (doctor.getSpecialization() == null || doctor.getSpecialization().trim().isEmpty()) {
			throw new IllegalArgumentException("Specialization is required.");
		}
		if (doctor.getExperience() < 0) {
			throw new IllegalArgumentException("Experience must be a non-negative number.");
		}
	}

	private void validatePatientDetails(Patient patient) {
		if (patient.getPatientName() == null || patient.getPatientName().trim().isEmpty()) {
			throw new IllegalArgumentException("Patient name is required.");
		}
		if (patient.getPatientName().length() < 3 || patient.getPatientName().length() > 50) {
			throw new IllegalArgumentException("Patient name must be between 3 and 50 characters.");
		}
		if (!patient.getPatientName().matches("^[a-zA-Z0-9 ]+$")) {
			throw new IllegalArgumentException("Patient name can only contain alphanumeric characters and spaces.");
		}
		if (patient.getContactNumber() == null || patient.getContactNumber().trim().isEmpty()) {
			throw new IllegalArgumentException("Contact number is required.");
		}
		if (!patient.getContactNumber().matches("^[0-9]{10}$")) {
			throw new IllegalArgumentException("Contact number must be a valid 10-digit number.");
		}
		if (patient.getDob() == null) {
			throw new IllegalArgumentException("Date of birth is required.");
		}
		/*
		 * if (patient.getGender() == null || patient.getGender().trim().isEmpty()) {
		 * throw new IllegalArgumentException("Gender is required."); }
		 */
		/*
		 * if (!patient.getGender().matches("^[MFmf]$")) { throw new
		 * IllegalArgumentException("Gender must be 'M' or 'F'."); }
		 */
		if (patient.getAddress() == null || patient.getAddress().trim().isEmpty()) {
			throw new IllegalArgumentException("Address is required.");
		}
	}

	@Override
	public Optional<UserDetails> findUserDetailsByName(String userName) {

		logger.info("Retrieving user details by userName: {}", userName);

		return userRepository.findByUserName(userName).map(user -> {
			UserDetails userDetails = new UserDetails();
			userDetails.setUserID(user.getUserID());
			userDetails.setPassword(user.getPassword());
			userDetails.setRoleID(user.getRole().getRoleID());
			userDetails.setUserName(user.getUserName());
			userDetails.setEmailID(user.getEmailID());

			Optional<Role> role = roleRepository.findById(user.getRole().getRoleID());
			role.ifPresent(r -> {
				switch (r.getRoleName().toUpperCase()) {
				case "DOCTOR":
					Optional<Doctor> doctorOptional = doctorRepository.findByUser(user);
					if (doctorOptional.isPresent())
						userDetails.setDoctor(doctorOptional.get());
					break;
				case "PATIENT":
					Optional<Patient> patientOptional = patientRepository.findByUser(user);
					if (patientOptional.isPresent())
						userDetails.setPatient(patientOptional.get());
					break;
				}
			});
			logger.debug("User Details::: {}", userDetails);
			return userDetails;
		});
	}

	@Override
	public Optional<User> findByUserName(String userName) {

		logger.info("Retrieving user by User Name: {}", userName);

		return userRepository.findByUserName(userName);
	}

}