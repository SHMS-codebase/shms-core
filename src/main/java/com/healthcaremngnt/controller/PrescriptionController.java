package com.healthcaremngnt.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.healthcaremngnt.constants.MessageConstants;
import com.healthcaremngnt.constants.RequestParamConstants;
import com.healthcaremngnt.enums.AppointmentStatus;
import com.healthcaremngnt.enums.Salutation;
import com.healthcaremngnt.exceptions.AppointmentNotFoundException;
import com.healthcaremngnt.exceptions.DataPersistenceException;
import com.healthcaremngnt.exceptions.PatientNotFoundException;
import com.healthcaremngnt.exceptions.TreatmentNotFoundException;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.MedicineDetail;
import com.healthcaremngnt.model.Patient;
import com.healthcaremngnt.model.Prescription;
import com.healthcaremngnt.model.PrescriptionDetail;
import com.healthcaremngnt.model.PrescriptionForm;
import com.healthcaremngnt.model.Treatment;
import com.healthcaremngnt.model.User;
import com.healthcaremngnt.repository.MedicineDetailRepository;
import com.healthcaremngnt.service.AppointmentService;
import com.healthcaremngnt.service.DoctorService;
import com.healthcaremngnt.service.PatientService;
import com.healthcaremngnt.service.PrescriptionDetailService;
import com.healthcaremngnt.service.PrescriptionService;
import com.healthcaremngnt.service.TreatmentService;
import com.healthcaremngnt.util.DateFormatter;

@Controller
@RequestMapping("/prescriptions")
public class PrescriptionController {

	private static final Logger logger = LogManager.getLogger(PrescriptionController.class);

	private final PrescriptionService prescriptionService;
	private final PrescriptionDetailService prescriptionDetailService;
	private final DoctorService doctorService;
	private final PatientService patientService;
	private final TreatmentService treatmentService;
	private final AppointmentService appointmentService;
	private final MedicineDetailRepository medicineDetailRepository;

	public PrescriptionController(PrescriptionService prescriptionService,
			PrescriptionDetailService prescriptionDetailService, DoctorService doctorService,
			PatientService patientService, TreatmentService treatmentService, AppointmentService appointmentService,
			MedicineDetailRepository medicineDetailRepository) {
		this.prescriptionService = prescriptionService;
		this.prescriptionDetailService = prescriptionDetailService;
		this.doctorService = doctorService;
		this.patientService = patientService;
		this.treatmentService = treatmentService;
		this.appointmentService = appointmentService;
		this.medicineDetailRepository = medicineDetailRepository;
	}

	@GetMapping("/viewprescription")
	public String viewPrescriptionDetails(@RequestParam(RequestParamConstants.PRESCRIPTION_ID) Long prescriptionID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Loading Prescription Details!!!");

		try {

			Prescription prescription = prescriptionService.getPrescriptionDetails(prescriptionID);

			List<PrescriptionDetail> prescriptionDetails = prescriptionDetailService
					.getPrescriptionDetails(prescription.getPrescriptionID());

			if (prescriptionDetails != null && !prescriptionDetails.isEmpty()) {
				prescription.setPrescriptionDetails(prescriptionDetails);
			}

			logger.debug("prescription: {}", prescription);
			model.addAttribute("prescription", prescription);

			// Get all medicine details from the repository
			List<MedicineDetail> medicineDetails = medicineDetailRepository.findAll(Sort.by("medicineName"));

			// Create a simplified data structure for JavaScript
			List<Map<String, Object>> simpleMedicineList = new ArrayList<>();
			for (MedicineDetail med : medicineDetails) {
				Map<String, Object> medicineMap = new HashMap<>();
				medicineMap.put("medicineID", med.getMedicineID());
				medicineMap.put("medicineName", med.getMedicineName());
				medicineMap.put("medicineType", med.getMedicineType());
				medicineMap.put("variation", med.getVariation());
				simpleMedicineList.add(medicineMap);
			}

			// Also create a simplified prescription details list
			List<Map<String, Object>> simplePrescriptionDetails = new ArrayList<>();
			if (prescription.getPrescriptionDetails() != null) {
				for (PrescriptionDetail detail : prescription.getPrescriptionDetails()) {
					Map<String, Object> detailMap = new HashMap<>();
					detailMap.put("medicineID", detail.getMedicine().getMedicineID());
					detailMap.put("medicineName", detail.getMedicine().getMedicineName());
					detailMap.put("medicineType", detail.getMedicine().getMedicineType());
					detailMap.put("dosage", detail.getDosage());
					detailMap.put("frequency", detail.getFrequency());
					detailMap.put("duration", detail.getDuration());
					detailMap.put("instructions", detail.getInstructions());
					simplePrescriptionDetails.add(detailMap);
				}
			}

			logger.debug("medicineDetails: {}", medicineDetails);
			logger.debug("simpleMedicineList: {}", simpleMedicineList);
			logger.debug("simplePrescriptionDetails: {}", simplePrescriptionDetails);

			// Add attributes to the model
			model.addAttribute("medicineDetails", medicineDetails);
			model.addAttribute("simpleMedicineList", simpleMedicineList);
			model.addAttribute("simplePrescriptionDetails", simplePrescriptionDetails);

		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.PRESCRIPTION_LOAD_ERROR, e.getLocalizedMessage());
			model.addAttribute("errorMessage", MessageConstants.PRESCRIPTION_LOAD_ERROR);

		}

		model.addAttribute("source", source);
		return "viewprescription";

	}

	@GetMapping("printprescription")
	public String viewPrintPrescription(@RequestParam(RequestParamConstants.PRESCRIPTION_ID) Long prescriptionID,
			Model model) {
		logger.info("Loading Print Prescription Details!!!");

		try {

			String currentDate = LocalDateTime.now().toString().replace("T", " ");

			Prescription prescription = prescriptionService.getPrescriptionDetails(prescriptionID);

			List<PrescriptionDetail> prescriptionDetails = prescriptionDetailService
					.getPrescriptionDetails(prescription.getPrescriptionID());

			if (prescriptionDetails != null && !prescriptionDetails.isEmpty()) {
				prescription.setPrescriptionDetails(prescriptionDetails);
			}

			logger.debug("prescription: {}", prescription);

			String formattedPrescriptionDate = DateFormatter
					.formatWithOrdinalSuffix(prescription.getPrescriptionDate());
			String formattedTreatmentDate = null;

			if (prescription.getTreatment() != null) {

				Treatment treatment = prescription.getTreatment();

				LocalDate treatmentDate = treatment.getTreatmentDate();
				LocalDateTime treatmentDateTime = treatmentDate.atStartOfDay();
				formattedTreatmentDate = DateFormatter.formatWithOrdinalSuffix(treatmentDateTime);

				Doctor doctor = doctorService.getDoctorDetails(treatment.getDoctorID());

				logger.debug("doctor: {}", doctor);
				model.addAttribute("doctor", doctor);

				Patient patient = patientService.getPatientDetails(treatment.getPatientID());
				logger.debug("patient: {}", patient);

				String salutation = (patient.getSalutation() == Salutation.CUSTOM) ? patient.getCustomSalutation()
						: patient.getSalutation().name();
				salutation = salutation.charAt(0) + salutation.substring(1).toLowerCase();

				model.addAttribute("salutation", salutation);
				model.addAttribute("patient", patient);

				User user = patient.getUser();
				logger.debug("user: {}", user);
				model.addAttribute("user", user);

			}

			model.addAttribute("formattedPrescriptionDate", formattedPrescriptionDate);
			model.addAttribute("formattedTreatmentDate", formattedTreatmentDate);
			model.addAttribute("currentDate", currentDate);
			model.addAttribute("prescription", prescription);

		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.PRESCRIPTION_PRINT_LOAD_ERROR, e.getLocalizedMessage());
			model.addAttribute("errorMessage", MessageConstants.PRESCRIPTION_PRINT_LOAD_ERROR);

		}

		return "printprescription";
	}

	@PostMapping("/updateprescription")
	public String updatePrescription(@ModelAttribute Prescription prescription,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Updating Prescriptions");

//		logger.debug("prescription: {}", prescription); // Log the entire object
		Optional.ofNullable(prescription).map(Prescription::getPrescriptionID)
				.ifPresent(id -> logger.debug("prescription ID from object: {}", id));
		Optional.ofNullable(prescription).map(Prescription::getPrescriptionDate)
				.ifPresent(id -> logger.debug("prescription Date from object: {}", id));
		Optional.ofNullable(prescription).map(Prescription::getPrescriptionStatus)
				.ifPresent(id -> logger.debug("prescription Status from object: {}", id));
		Optional.ofNullable(prescription).map(Prescription::getGeneralInstructions)
				.ifPresent(id -> logger.debug("General Instructions from object: {}", id));

		Optional.ofNullable(source).ifPresent(src -> logger.debug("Source: {}", src));

		if (prescription.getTreatment() != null) {
			logger.debug("Treatment is not null");

		} else {
			logger.debug("Treatment is null");
		}

		if (prescription.getPrescriptionDetails() != null) {
			logger.debug("Prescription Details is not null");

			for (PrescriptionDetail prescriptionDetail : prescription.getPrescriptionDetails()) {
				logger.debug("ID: {}", prescriptionDetail.getPrescriptionDetailID());
				logger.debug("Medicine: {}, {}, {}", prescriptionDetail.getMedicine().getMedicineID(),
						prescriptionDetail.getMedicine().getMedicineName(),
						prescriptionDetail.getMedicine().getMedicineType());
				logger.debug("Dosage: {}", prescriptionDetail.getDosage());
				logger.debug("Frequency: {}", prescriptionDetail.getFrequency());
				logger.debug("Duration: {}", prescriptionDetail.getDuration());
				logger.debug("Instructions: {}", prescriptionDetail.getInstructions());

			}

		} else {
			logger.debug("Prescription Details is null");

		}
		try {
			prescriptionService.updatePrescriptionDetails(prescription);
			logger.debug("{}", MessageConstants.PRESCRIPTION_UPD_SUCCESS);
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.PRESCRIPTION_UPD_FAILURE, e);
			model.addAttribute("errorMessage", MessageConstants.PRESCRIPTION_UPD_FAILURE);
			model.addAttribute("prescription", prescription);
			return "viewprescription";
		}

		model.addAttribute("prescription", prescription);
		model.addAttribute("prescriptionID", prescription.getPrescriptionID());
		model.addAttribute("message", MessageConstants.PRESCRIPTION_UPD_SUCCESS);
		model.addAttribute("source", source);

		return "viewprescription";
	}

	@GetMapping("/createprescription")
	public String viewCreatePrescription(@ModelAttribute(RequestParamConstants.APPOINTMENT_ID) Long appointmentID,
			@ModelAttribute(RequestParamConstants.SAVED_TREATMENT) Treatment savedTreatment,
			@ModelAttribute(RequestParamConstants.SOURCE) String source, Model model, Authentication authentication)
			throws AppointmentNotFoundException, PatientNotFoundException {

		logger.info("Loading Create Prescription!!!");

		if (logger.isDebugEnabled()) {
			logger.debug("Request parameters - appointmentID: {},  source: {}, savedTreatment: {}", appointmentID,
					source, savedTreatment);
		}

		Patient patient = new Patient();
		Long patientID = null;

		if (savedTreatment != null && savedTreatment.getAppointment() != null) {

			patientID = savedTreatment.getPatientID();
			logger.debug("patientID: {}", patientID);

			patient = patientService.getPatientDetails(patientID);
			logger.debug("patient: {}", patient);

		}

		// Get all medicine details from the repository
		List<MedicineDetail> medicineDetails = medicineDetailRepository.findAll(Sort.by("medicineName"));

		// Structure data to support cascading dropdowns
		Map<String, Map<String, List<String>>> uniqueMedicineNames = new LinkedHashMap<>();

		medicineDetails.forEach(medicine -> {
			uniqueMedicineNames.computeIfAbsent(medicine.getMedicineName(), k -> new LinkedHashMap<>())
					.computeIfAbsent(medicine.getMedicineType(), k -> new ArrayList<>()).add(medicine.getVariation());
		});

		logger.debug("medicineDetails: {}", medicineDetails);
		logger.debug("uniqueMedicineNames: {}", uniqueMedicineNames);

		// Add attributes to the model
		model.addAttribute("medicineDetails", medicineDetails);
		model.addAttribute("uniqueMedicineNames", uniqueMedicineNames);

		if (!model.containsAttribute("prescriptionForm")) {
			PrescriptionForm prescriptionForm = new PrescriptionForm();
			prescriptionForm.setPrescriptionDetails(new ArrayList<>()); // Initialize the list
			model.addAttribute("prescriptionForm", prescriptionForm);
		}

		model.addAttribute("patient", patient);
		model.addAttribute("source", source);
		model.addAttribute("appointmentID", appointmentID);
		model.addAttribute("treatment", savedTreatment); // Add the saved treatment to the model
		model.addAttribute("isSaved", false);

		return "createprescription";
	}

	@PostMapping("/saveprescription")
	public String savePrescription(
			@RequestParam(value = RequestParamConstants.APPOINTMENT_ID, required = false) Long appointmentID,
			@RequestParam(value = RequestParamConstants.TREATMENT_ID, required = false) Long treatmentID,
			@ModelAttribute PrescriptionForm prescriptionForm,
			@RequestParam(value = RequestParamConstants.SOURCE, required = false) String source,
			RedirectAttributes redirectAttributes, Model model, Authentication authentication)
			throws AppointmentNotFoundException, TreatmentNotFoundException, DataPersistenceException {
		logger.info("Creating Prescriptions!!!");

		if (logger.isDebugEnabled()) {
			logger.debug("Request parameters - appointmentID: {}, treatmentID: {}, source: {}, prescriptionForm: {}",
					appointmentID, treatmentID, source, prescriptionForm);
		}

		Prescription prescription = new Prescription();

		if (treatmentID != null) {
			Treatment treatment = treatmentService.getTreatmentDetails(treatmentID);
			prescription.setTreatment(treatment);
		}

		if (prescriptionForm != null) {
			logger.debug("prescriptionForm is not null!!");
			prescription.setPrescriptionDate(prescriptionForm.getPrescriptionDate());
			prescription.setPrescriptionStatus(prescriptionForm.getPrescriptionStatus());
			prescription.setGeneralInstructions(prescriptionForm.getGeneralInstructions());
		}

		Prescription savedPrescription = new Prescription();

		try {

			if (prescriptionForm.getPrescriptionDetails() != null
					&& !prescriptionForm.getPrescriptionDetails().isEmpty()) {
				logger.debug("Creating {} prescription records", prescriptionForm.getPrescriptionDetails().size());
				savedPrescription = prescriptionService.createPrescription(prescription,
						prescriptionForm.getPrescriptionDetails());
				logger.debug("savedPrescription: {}", savedPrescription);
				logger.debug("Update the status of Appointment with ID: {}", appointmentID);
				appointmentService.updateAppointmentStatus(appointmentID, AppointmentStatus.COMPLETED);
			}

			logger.debug("{}", MessageConstants.PRESCRIPTION_CRTED_SUCCESS);

			model.addAttribute("prescriptionForm", prescriptionForm);
			model.addAttribute("message", MessageConstants.PRESCRIPTION_CRTED_SUCCESS);

		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.PRESCRIPTION_CRTED_FAILURE, e);
			model.addAttribute("errorMessage", MessageConstants.PRESCRIPTION_CRTED_FAILURE);
		}

		model.addAttribute("source", source);
		model.addAttribute("prescription", savedPrescription);

//		return "redirect:/prescriptions/viewprescription?prescriptionID=" + prescriptionID + "&source=" + source;
		return "viewprescription";
	}

}