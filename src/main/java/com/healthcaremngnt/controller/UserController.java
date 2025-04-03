package com.healthcaremngnt.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.healthcaremngnt.constants.MessageConstants;
import com.healthcaremngnt.constants.RequestParamConstants;
import com.healthcaremngnt.exceptions.EmailAlreadyExistsException;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.Patient;
import com.healthcaremngnt.model.RegistrationForm;
import com.healthcaremngnt.model.Role;
import com.healthcaremngnt.model.User;
import com.healthcaremngnt.model.UserDetails;
import com.healthcaremngnt.service.DoctorService;
import com.healthcaremngnt.service.EmailService;
import com.healthcaremngnt.service.PatientService;
import com.healthcaremngnt.service.RoleService;
import com.healthcaremngnt.service.UserService;
import com.healthcaremngnt.util.UserDetailsGenerator;

@Controller
@RequestMapping("/users")
public class UserController {

	private static final Logger logger = LogManager.getLogger(UserController.class);

	private final RoleService roleService;
	private final UserService userService;
	private final DoctorService doctorService;
	private final PatientService patientService;
	private final EmailService emailService;

	public UserController(RoleService roleService, UserService userService, DoctorService doctorService,
			PatientService patientService, EmailService emailService) {
		this.roleService = roleService;
		this.userService = userService;
		this.doctorService = doctorService;
		this.patientService = patientService;
		this.emailService = emailService;
	}

	@GetMapping("/register")
	public String showRegistrationForm(@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("The Registration Form is loaded");
		model.addAttribute("user", new User());
		model.addAttribute("source", source);

		return "register";
	}

	@PostMapping("/register")
	public String registerUser(@ModelAttribute RegistrationForm form,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Processing user registration");

		// Validate form data
		if (form == null) {
			model.addAttribute("errorMessage", MessageConstants.REG_FORM_EMPTY);
			return "register";
		} else
			model.addAttribute("source", source);

		if (logger.isDebugEnabled()) {
			loginFormDetails(form);
		}

		String roleName = form.getRole();
		String userName = getUserName(form, roleName);
		String emailID = getEmailID(form, roleName);

		// Validate role
		Role role = roleService.getRoleByName(roleName);

		// Create user
		String generatedUserName = UserDetailsGenerator.generateUserName(userName, roleName);
		String generatedPassword = UserDetailsGenerator.generatePassword(userName, roleName);

		User user = new User();
		user.setRole(role);
		user.setEmailID(emailID);
		user.setUserName(generatedUserName);
		user.setPassword(generatedPassword);

		// Register user
		try {
			user = userService.register(user);

			// Handle Admin role - no additional profile needed
			if (roleName.equalsIgnoreCase("ADMIN")) {
				return completeRegistration(emailID, userName, generatedUserName, generatedPassword, model,
						MessageConstants.ADMIN_REG_SUCCESS);
			}

			// Create profile based on role
			if (roleName.equalsIgnoreCase("DOCTOR")) {
				return registerDoctor(form, user, emailID, userName, generatedUserName, generatedPassword, model);
			} else if (roleName.equalsIgnoreCase("PATIENT")) {
				return registerPatient(form, user, emailID, userName, generatedUserName, generatedPassword, model);
			}

		} catch (EmailAlreadyExistsException e) {
			logger.error("{}: {}", MessageConstants.EMAIL_ID_ALREADY_EXISTS, e);
			model.addAttribute("errorMessage", MessageConstants.EMAIL_ID_ALREADY_EXISTS);
			return "register";
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.USER_REGISTER_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.USER_REGISTER_ERROR);
			return "register";
		}

		// Fallback case (should not reach here if roles are properly defined)
//		model.addAttribute("errorMessage", "Unsupported role: " + roleName);

		return "register";
	}

	private void loginFormDetails(RegistrationForm form) {
		logger.info("Registration form details:");
		logger.debug("Role: {}, Admin Name: {}, Admin Email: {}", form.getRole(), form.getAdminName(),
				form.getAdminEmailID());
		logger.debug("Doctor - Name: {}, Email: {}, Address: {}, Contact: {}", form.getDoctorName(),
				form.getDoctorEmailID(), form.getDoctorAddress(), form.getDoctorContactNumber());
		logger.debug("Doctor - Qualification: {}, Specialization: {}, Experience: {}", form.getQualification(),
				form.getSpecialization(), form.getExperience());
		logger.debug("Patient - Name: {}, Email: {}, Address: {}, Contact: {}", form.getPatientName(),
				form.getPatientEmailID(), form.getPatientAddress(), form.getPatientContactNumber());
		logger.debug("Patient - DOB: {}, Gender: {}", form.getDob(), form.getGender());
	}

	private String getUserName(RegistrationForm form, String roleName) {
		logger.info("Get User Name");
		if (roleName.equalsIgnoreCase("ADMIN")) {
			return form.getAdminName();
		} else if (roleName.equalsIgnoreCase("DOCTOR")) {
			return form.getDoctorName();
		} else if (roleName.equalsIgnoreCase("PATIENT")) {
			return form.getPatientName();
		}
		return null;
	}

	private String getEmailID(RegistrationForm form, String roleName) {
		logger.info("Get Email ID");
		if (roleName.equalsIgnoreCase("ADMIN")) {
			return form.getAdminEmailID();
		} else if (roleName.equalsIgnoreCase("DOCTOR")) {
			return form.getDoctorEmailID();
		} else if (roleName.equalsIgnoreCase("PATIENT")) {
			return form.getPatientEmailID();
		}
		return null;
	}

	private String registerDoctor(RegistrationForm form, User user, String emailID, String userName,
			String generatedUserName, String generatedPassword, Model model) {
		logger.info("Creating doctor profile");

		Doctor doctor = new Doctor();
		String doctorName = form.getDoctorName();

		// Add "Dr." prefix if not already present
		if (doctorName != null && !doctorName.startsWith("Dr.")) {
			doctorName = MessageConstants.DOCTOR_TITLE + doctorName;
		}

		doctor.setDoctorName(doctorName);
		doctor.setContactNumber(form.getDoctorContactNumber());
		doctor.setAddress(form.getDoctorAddress());
		doctor.setQualification(form.getQualification());
		doctor.setSpecialization(form.getSpecialization());
		doctor.setExperience(Integer.parseInt(form.getExperience()));
		doctor.setUser(user);

		try {
			doctorService.register(doctor);
			return completeRegistration(emailID, userName, generatedUserName, generatedPassword, model,
					MessageConstants.DOCTOR_REG_SUCCESS);
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.DOCTOR_REG_FAILURE, e);
			model.addAttribute("errorMessage", MessageConstants.DOCTOR_REG_FAILURE);
			return "register";
		}
	}

	private String registerPatient(RegistrationForm form, User user, String emailID, String userName,
			String generatedUserName, String generatedPassword, Model model) {
		logger.info("Creating patient profile");

		Patient patient = new Patient();
		patient.setPatientName(form.getPatientName());
		patient.setDob(form.getDob());
		patient.setGender(form.getGender());
		patient.setContactNumber(form.getPatientContactNumber());
		patient.setAddress(form.getPatientAddress());
		patient.setUser(user);

		try {
			patientService.register(patient);
			return completeRegistration(emailID, userName, generatedUserName, generatedPassword, model,
					MessageConstants.PATIENT_REG_SUCCESS);
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.PATIENT_REG_FAILURE, e);
			model.addAttribute("errorMessage", MessageConstants.PATIENT_REG_FAILURE);
			return "register";
		}
	}

	private String completeRegistration(String emailID, String userName, String generatedUserName,
			String generatedPassword, Model model, String successMessage) {

		emailService.sendRegistrationEmail(emailID, userName, generatedUserName, generatedPassword);
		model.addAttribute("message", successMessage);
		model.addAttribute("source", "admindashboard");
		return "registrationsuccess";
	}

	@GetMapping("/viewuser")
	public String showUserForm(@RequestParam(RequestParamConstants.USER_ID) Long userID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("View User");

		try {
			UserDetails userDetails = userService.findUserDetailsByID(userID);
			if (userDetails != null) {
				model.addAttribute("userDetails", userDetails);
			} else {
				model.addAttribute("errorMessage", MessageConstants.USER_NOT_FOUND);
			}
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.USER_FETCH_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.USER_FETCH_ERROR);
		}

		model.addAttribute("source", source);
		return "viewuser";
	}

	@PostMapping("/updateuser")
	public String updateUser(@ModelAttribute UserDetails userDetails,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Update User");

		try {
			logUserDetails(userDetails);
			userService.updateUserDetails(userDetails);
			model.addAttribute("userDetails", userDetails);
			model.addAttribute("message", MessageConstants.USER_UPD_SUCCESS);
			logger.debug("{}", MessageConstants.USER_UPD_SUCCESS);
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.USER_UPD_FAILURE, e.getLocalizedMessage());
			model.addAttribute("errorMessage", MessageConstants.USER_UPD_FAILURE);
			model.addAttribute("userDetails", userDetails);
			return "viewuser";
		}

		model.addAttribute("source", source);
		return "viewuser";
	}

	@GetMapping("/edituserprofile")
	public String viewEditProfile(@SessionAttribute(RequestParamConstants.USER_NAME) String userName, Model model,
			@RequestParam(RequestParamConstants.SOURCE) String source) {
		logger.info("Loading Edit User Profile!!!");

		logger.debug("userName: {}", userName);
		logger.debug("source: {}", source);

		try {
			UserDetails userDetails = userService.findUserDetailsByName(userName);

			if (userDetails != null) {
				model.addAttribute("userDetails", userDetails);
				logger.debug("{}", MessageConstants.EDIT_USER_FETCH_SUCCESS);
			} else {
				logger.error("{}", MessageConstants.USER_NOT_FOUND);
				model.addAttribute("errorMessage", MessageConstants.USER_NOT_FOUND);
			}

			model.addAttribute("source", source);
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.EDIT_USER_FETCH_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.EDIT_USER_FETCH_ERROR);
		}

		model.addAttribute("source", source);
		return "edituserprofile";
	}

	@PostMapping("/edituserprofile")
	public String updateUserProfile(@ModelAttribute UserDetails userDetails, Model model,
			@RequestParam(RequestParamConstants.SOURCE) String source) {
		logger.info("Updating User Profile!!!");

		try {
			logUserDetails(userDetails);
			userService.updateUserDetails(userDetails);
			logger.debug("{}", MessageConstants.USER_UPD_SUCCESS);
			model.addAttribute("userDetails", userDetails);
			model.addAttribute("message", MessageConstants.USER_UPD_SUCCESS);
			model.addAttribute("source", source);

		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.USER_UPD_FAILURE, e.getLocalizedMessage());
			model.addAttribute("errorMessage", MessageConstants.USER_UPD_FAILURE);
			model.addAttribute("userDetails", userDetails);
			model.addAttribute("source", source);
			return "edituserprofile";
		}

		return "edituserprofile";
	}

	private void logUserDetails(UserDetails userDetails) {
		if (userDetails != null) {
			logger.debug("UserDetails is available");
			logger.debug("Role ID       ::: {}", userDetails.getRoleID());
			logger.debug("User ID       ::: {}", userDetails.getUserID());
			logger.debug("User Name     ::: {}", userDetails.getUserName());
			logger.debug("Email ID      ::: {}", userDetails.getEmailID());
			logger.debug("Password      ::: {}", userDetails.getPassword());
			logger.debug("Doctor        ::: {}", userDetails.getDoctor());
			logger.debug("Patient       ::: {}", userDetails.getPatient());
		}
	}

}