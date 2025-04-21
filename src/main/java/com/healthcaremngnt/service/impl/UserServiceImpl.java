package com.healthcaremngnt.service.impl;

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
	public User register(User user) throws UsernameAlreadyExistsException {
		logger.info("Registering user: {}", user.getUserName());

		validateUserData(user);

		user.setPassword(encoder.encode(user.getPassword()));
		logger.debug("Encoded Generated Password: {}", user.getPassword());

		return saveUser(user);
	}

	private void validateUserData(User user) throws UsernameAlreadyExistsException {
		if (user == null) {
			throw new IllegalArgumentException("User object cannot be null.");
		}

		if (userRepository.findByUserName(user.getUserName()).isPresent()) {
			throw new UsernameAlreadyExistsException("Username already exists.");
		}
	}

	private User saveUser(User user) {
		try {
			return userRepository.save(user);
		} catch (DataIntegrityViolationException e) {
			if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
				throw new EmailAlreadyExistsException("Email ID already exists.");
			}
			logger.error("Unexpected error while registering user: {}", e.getMessage(), e);
			throw new RuntimeException("An unexpected error occurred while registering the user", e);
		}
	}

	@Override
	public User findByEmailID(String email) {
		logger.info("Fetching user by email ID: {}", email);

		validateEmail(email);

		return userRepository.findByEmailID(email)
				.orElseThrow(() -> new RuntimeException("User not found with email ID: " + email));
	}

	private void validateEmail(String email) {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Email ID cannot be null or empty.");
		}
	}

	@Override
	public UserDetails findUserDetailsByID(Long userID) {
		logger.info("Fetching user details for User ID: {}", userID);

		validateUserID(userID);

		User user = userRepository.findById(userID)
				.orElseThrow(() -> new RuntimeException("User not found with ID: " + userID));

		UserDetails userDetails = mapUserToUserDetails(user);

		logger.debug("Retrieved User Details: {}", userDetails);
		return userDetails;
	}

	private void validateUserID(Long userID) {
		if (userID == null || userID <= 0) {
			throw new IllegalArgumentException("Invalid User ID provided.");
		}
	}

	private UserDetails mapUserToUserDetails(User user) {
		UserDetails userDetails = new UserDetails();
		userDetails.setUserID(user.getUserID());
		userDetails.setRoleID(user.getRole().getRoleID());
		userDetails.setUserName(user.getUserName());
		userDetails.setEmailID(user.getEmailID());

		Role role = roleRepository.findById(user.getRole().getRoleID())
				.orElseThrow(() -> new RuntimeException("Role not found for User ID: " + user.getUserID()));

		setRoleSpecificDetails(userDetails, role, user);
		return userDetails;
	}

	private void setRoleSpecificDetails(UserDetails userDetails, Role role, User user) {
		switch (role.getRoleName().toUpperCase()) {
		case "DOCTOR":
			doctorRepository.findByUser(user).ifPresent(userDetails::setDoctor);
			break;
		case "PATIENT":
			patientRepository.findByUser(user).ifPresent(userDetails::setPatient);
			break;
		default:
			logger.warn("No specific details found for role: {}", role.getRoleName());
			break;
		}
	}

	@Override
	public void updateUserDetails(UserDetails userDetails) {
		logger.info("Updating user details for User ID: {}", userDetails.getUserID());

		validateUserDetails(userDetails);

		User user = userRepository.findById(userDetails.getUserID())
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userDetails.getUserID()));

		updateBasicUserDetails(user, userDetails);
		userRepository.save(user);

		updateRoleSpecificDetails(userDetails);
	}

	private void updateBasicUserDetails(User user, UserDetails userDetails) {
		user.setUserName(userDetails.getUserName());
		user.setEmailID(userDetails.getEmailID());
	}

	private void updateRoleSpecificDetails(UserDetails userDetails) {
		if (userDetails.getDoctor() != null) {
			Doctor existingDoctor = doctorRepository.findById(userDetails.getDoctor().getDoctorID()).orElseThrow(
					() -> new IllegalArgumentException("Doctor not found: " + userDetails.getDoctor().getDoctorID()));

			updateDoctorDetails(existingDoctor, userDetails.getDoctor());
			doctorRepository.save(existingDoctor);
		}

		if (userDetails.getPatient() != null) {
			Patient existingPatient = patientRepository.findById(userDetails.getPatient().getPatientID())
					.orElseThrow(() -> new IllegalArgumentException(
							"Patient not found: " + userDetails.getPatient().getPatientID()));

			updatePatientDetails(existingPatient, userDetails.getPatient());
			patientRepository.save(existingPatient);
		}
	}

	private void updateDoctorDetails(Doctor doctor, Doctor updatedDoctor) {
		doctor.setDoctorName(updatedDoctor.getDoctorName());
		doctor.setContactNumber(updatedDoctor.getContactNumber());
		doctor.setAddress(updatedDoctor.getAddress());
		doctor.setQualification(updatedDoctor.getQualification());
		doctor.setSpecialization(updatedDoctor.getSpecialization());
		doctor.setExperience(updatedDoctor.getExperience());
		doctor.setLicenseNumber(updatedDoctor.getLicenseNumber());
	}

	private void updatePatientDetails(Patient patient, Patient updatedPatient) {
		patient.setPatientName(updatedPatient.getPatientName());
		patient.setDob(updatedPatient.getDob());
		patient.setGender(updatedPatient.getGender());
		patient.setContactNumber(updatedPatient.getContactNumber());
		patient.setAddress(updatedPatient.getAddress());
	}

	private void validateUserDetails(UserDetails userDetails) {
		logger.info("Validating user details: {}", userDetails);

		if (userDetails == null) {
			throw new IllegalArgumentException("UserDetails object cannot be null.");
		}

		validateUserBasicInfo(userDetails);

		if (userDetails.getDoctor() != null) {
			validateDoctorDetails(userDetails.getDoctor());
		}

		if (userDetails.getPatient() != null) {
			validatePatientDetails(userDetails.getPatient());
		}
	}

	private void validateUserBasicInfo(UserDetails userDetails) {
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
	}

	private void validateDoctorDetails(Doctor doctor) {
		logger.info("Validating doctor details: {}", doctor);

		if (doctor == null) {
			throw new IllegalArgumentException("Doctor object cannot be null.");
		}

		validateDoctorName(doctor.getDoctorName());
		validateContactNumber(doctor.getContactNumber());
		validateRequiredField(doctor.getQualification(), "Qualification");
		validateRequiredField(doctor.getSpecialization(), "Specialization");

		if (doctor.getExperience() < 0) {
			throw new IllegalArgumentException("Experience must be a non-negative number.");
		}
	}

	private void validateDoctorName(String doctorName) {
		if (doctorName == null || doctorName.trim().isEmpty()) {
			throw new IllegalArgumentException("Doctor name is required.");
		}
		if (!doctorName.matches("^Dr\\.\\s?[a-zA-Z0-9 ]+$")) {
			throw new IllegalArgumentException(
					"Doctor name must start with 'Dr.' and contain alphanumeric characters.");
		}
	}

	private void validateContactNumber(String contactNumber) {
		if (contactNumber == null || contactNumber.trim().isEmpty()) {
			throw new IllegalArgumentException("Contact number is required.");
		}
		if (!contactNumber.matches("^[0-9]{10}$")) {
			throw new IllegalArgumentException("Contact number must be a valid 10-digit number.");
		}
	}

	private void validateRequiredField(String fieldValue, String fieldName) {
		if (fieldValue == null || fieldValue.trim().isEmpty()) {
			throw new IllegalArgumentException(fieldName + " is required.");
		}
	}

	private void validatePatientDetails(Patient patient) {
		logger.info("Validating patient details: {}", patient);

		if (patient == null) {
			throw new IllegalArgumentException("Patient object cannot be null.");
		}

		validatePatientName(patient.getPatientName());
		validateContactNumber(patient.getContactNumber());
		validateRequiredField(patient.getAddress(), "Address");

		if (patient.getDob() == null) {
			throw new IllegalArgumentException("Date of birth is required.");
		}
	}

	private void validatePatientName(String patientName) {
		if (patientName == null || patientName.trim().isEmpty()) {
			throw new IllegalArgumentException("Patient name is required.");
		}
		if (!patientName.matches("^[a-zA-Z0-9 ]+$")) {
			throw new IllegalArgumentException("Patient name can only contain alphanumeric characters and spaces.");
		}
	}

	@Override
	public UserDetails findUserDetailsByName(String userName) {
		logger.info("Fetching user details for username: {}", userName);

		validateUserName(userName);

		User user = userRepository.findByUserName(userName)
				.orElseThrow(() -> new RuntimeException("User not found with username: " + userName));

		UserDetails userDetails = mapUserToUserDetails(user);

		logger.debug("Retrieved User Details: {}", userDetails);
		return userDetails;
	}

	private void validateUserName(String userName) {
		if (userName == null || userName.trim().isEmpty()) {
			throw new IllegalArgumentException("Username cannot be null or empty.");
		}
	}

	@Override
	public User findByUserName(String userName) {
		logger.info("Fetching user by User Name: {}", userName);

		validateUserName(userName);

		return userRepository.findByUserName(userName)
				.orElseThrow(() -> new RuntimeException("User not found with username: " + userName));
	}

}