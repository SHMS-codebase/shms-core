package com.healthcaremngnt.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import com.healthcaremngnt.exceptions.AppointmentNotFoundException;
import com.healthcaremngnt.exceptions.DataPersistenceException;
import com.healthcaremngnt.exceptions.PatientNotFoundException;
import com.healthcaremngnt.exceptions.TreatmentNotFoundException;
import com.healthcaremngnt.model.MedicineDetail;
import com.healthcaremngnt.model.Patient;
import com.healthcaremngnt.model.Prescription;
import com.healthcaremngnt.model.PrescriptionDetail;
import com.healthcaremngnt.model.PrescriptionForm;
import com.healthcaremngnt.model.Treatment;
import com.healthcaremngnt.repository.MedicineDetailRepository;
import com.healthcaremngnt.service.AppointmentService;
import com.healthcaremngnt.service.PatientService;
import com.healthcaremngnt.service.PrescriptionDetailService;
import com.healthcaremngnt.service.PrescriptionService;
import com.healthcaremngnt.service.TreatmentService;

@Controller
@RequestMapping("/prescriptions")
public class PrescriptionController {

	private static final Logger logger = LogManager.getLogger(PrescriptionController.class);

	private final PrescriptionService prescriptionService;
	private final PrescriptionDetailService prescriptionDetailService;
	private final PatientService patientService;
	private final TreatmentService treatmentService;
	private final AppointmentService appointmentService;

	private final MedicineDetailRepository medicineDetailRepository;

	public PrescriptionController(PrescriptionService prescriptionService,
			PrescriptionDetailService prescriptionDetailService, PatientService patientService,
			TreatmentService treatmentService, AppointmentService appointmentService,
			MedicineDetailRepository medicineDetailRepository) {
		this.prescriptionService = prescriptionService;
		this.prescriptionDetailService = prescriptionDetailService;
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

			model.addAttribute("prescription", prescription);
			logger.debug("prescription: {}", prescription);

		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.PRESCRIPTION_LOAD_ERROR, e.getLocalizedMessage());
			model.addAttribute("errorMessage", MessageConstants.PRESCRIPTION_LOAD_ERROR);

		}

		model.addAttribute("source", source);
		return "viewprescription";

	}

	@PostMapping("/updateprescription")
	public String updatePrescription(@ModelAttribute Prescription prescription,
			@RequestParam(value = RequestParamConstants.PRESCRIPTION_ID, required = false) Long prescriptionID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Updating Prescriptions");

		Optional.ofNullable(prescription).ifPresent(id -> logger.debug("prescription: {}", id));
		Optional.ofNullable(prescriptionID).ifPresent(id -> logger.debug("prescription ID: {}", id));
		Optional.ofNullable(source).ifPresent(src -> logger.debug("Source: {}", src));

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
		model.addAttribute("prescriptionID", prescriptionID);
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
		List<MedicineDetail> medicineDetails = medicineDetailRepository.findAll();

		// Create a map of medicine IDs to medicine names
		Map<Long, String> uniqueMedicineNames = new LinkedHashMap<>();

		// Group by medicine name AND type to avoid duplicates in dropdown
		medicineDetails.stream()
				.collect(Collectors
						.groupingBy(medicine -> medicine.getMedicineName() + " - " + medicine.getMedicineType()))
				.forEach((nameWithType, medicines) -> {
					// Just take the first medicine's ID for each unique name-type combination
					uniqueMedicineNames.put(medicines.get(0).getMedicineID(), nameWithType);
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