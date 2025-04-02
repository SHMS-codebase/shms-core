package com.healthcaremngnt.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.healthcaremngnt.constants.MessageConstants;
import com.healthcaremngnt.constants.RequestParamConstants;
import com.healthcaremngnt.enums.InvoiceStatus;
import com.healthcaremngnt.enums.TreatmentStatus;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.Invoice;
import com.healthcaremngnt.model.Patient;
import com.healthcaremngnt.model.Prescription;
import com.healthcaremngnt.model.Treatment;
import com.healthcaremngnt.model.User;
import com.healthcaremngnt.service.DoctorService;
import com.healthcaremngnt.service.InvoiceService;
import com.healthcaremngnt.service.PatientService;
import com.healthcaremngnt.service.PrescriptionService;
import com.healthcaremngnt.service.TreatmentService;
import com.healthcaremngnt.util.DateFormatter;

@Controller
@RequestMapping("/invoices")
public class InvoiceController {

	private static final Logger logger = LogManager.getLogger(InvoiceController.class);

	private final InvoiceService invoiceService;
	private final TreatmentService treatmentService;
	private final PrescriptionService prescriptionService;
	private final DoctorService doctorService;
	private final PatientService patientService;

	public InvoiceController(InvoiceService invoiceService, TreatmentService treatmentService,
			PrescriptionService prescriptionService, DoctorService doctorService, PatientService patientService) {
		this.invoiceService = invoiceService;
		this.treatmentService = treatmentService;
		this.prescriptionService = prescriptionService;
		this.doctorService = doctorService;
		this.patientService = patientService;
	}

	@GetMapping("/unbilledtreatments")
	public String viewUnbilledTreatments(@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {

		logger.info("Loading all unbilled treatments for Invoice Generation!!");

		try {
			List<Treatment> unbilledTreatments = treatmentService.getAllUnbilledTreatments();
			model.addAttribute("unbilledTreatments", unbilledTreatments);
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.UNBILLED_TREATMENTS_LOAD_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.UNBILLED_TREATMENTS_LOAD_ERROR);
			return source;
		}

		model.addAttribute("source", source);

		return "unbilledtreatments";
	}

	@GetMapping("/generate-invoice")
	public String viewGenerateInvoice(@RequestParam(RequestParamConstants.TREATMENT_ID) Long treatmentID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Loading the Generate Invoice Page!!");

		Optional<Treatment> treatmentOptional = treatmentService.getTreatmentDetails(treatmentID);
		if (!treatmentOptional.isPresent()) {
			model.addAttribute("errorMessage", MessageConstants.TREATMENT_NOT_FOUND);
			return source;
		}

		Treatment treatment = treatmentOptional.get();
		model.addAttribute("treatment", treatment);
		model.addAttribute("source", source);

		Optional<Prescription> prescriptionOptional = prescriptionService
				.getPrescriptionDetailsByTreatment(treatmentID);

		if (prescriptionOptional.isPresent()) {
			Prescription prescription = prescriptionOptional.get();
			model.addAttribute("prescription", prescription);
		} else {
			// Either add an empty prescription or add a flag indicating no prescription
			// exists
			model.addAttribute("prescription", new Prescription());
			model.addAttribute("noPrescription", true);
		}

		return "generateinvoice";
	}

	@PostMapping("/generate-invoice")
	public String generateInvoice(@RequestParam(RequestParamConstants.TREATMENT_ID) Long treatmentID,
			@RequestParam(value = RequestParamConstants.PRESCRIPTION_ID, required = false) Long prescriptionID,
			@RequestParam(RequestParamConstants.TREATMENT_COST) BigDecimal treatmentCost,
			@RequestParam(value = RequestParamConstants.PRESCRIPTION_COST, required = false) BigDecimal prescriptionCost,
			@RequestParam(RequestParamConstants.TOTAL_AMOUNT) BigDecimal totalAmount,
			@RequestParam(RequestParamConstants.INVOICE_STATUS) InvoiceStatus invoiceStatus,
			@RequestParam(value = RequestParamConstants.SOURCE, required = false) String source, RedirectAttributes redirectAttributes,
			Model model) {

		logger.info("Generating Invoice!!");

		if (logger.isDebugEnabled()) {
			logger.debug(
					"Request parameters - treatmentID: {},  prescriptionID: {}, treatmentCost: {}, prescriptionCost: {}, totalAmount: {}, invoiceStatus: {}, source: {}",
					treatmentID, prescriptionID, treatmentCost, prescriptionCost, totalAmount, invoiceStatus, source);
		}

		try {

			// Create a new invoice entity
			Invoice invoice = new Invoice();

			invoice.setTreatmentCost(treatmentCost);
			invoice.setPrescriptionCost(prescriptionCost);
			invoice.setTotalAmount(totalAmount);
			if (invoiceStatus.equals(InvoiceStatus.DRAFT))
				invoice.setInvoiceStatus(InvoiceStatus.REVIEWED);
			else if (invoiceStatus.equals(InvoiceStatus.REVIEWED))
				invoice.setInvoiceStatus(InvoiceStatus.FINALIZED);
			invoice.setInvoiceDate(LocalDateTime.now());

			// Retrieve the treatment entity
			Optional<Treatment> treatmentOptional = treatmentService.getTreatmentDetails(treatmentID);
			Treatment treatment = new Treatment();

			if (treatmentOptional.isPresent()) {
				treatment = treatmentOptional.get();
				logger.debug("treatment: {}", treatment);
				invoice.setTreatment(treatment);
			}

			// Retrieve the prescription entity if prescriptionID is not null or empty
			Optional<Prescription> prescriptionOptional = null;
			Prescription prescription = new Prescription();

			if (prescriptionID != null && prescriptionID > 0) {
				prescriptionOptional = prescriptionService.getPrescriptionDetails(prescriptionID);
				if (prescriptionOptional.isPresent()) {
					prescription = prescriptionOptional.get();
					logger.debug("prescription: {}", prescription);
					invoice.setPrescription(prescription);
				}
			}

			logger.debug("invoice: {}", invoice);

			// Generate invoice number (you may implement your own logic)
//			String invoiceNumber = "INV-" + System.currentTimeMillis();
//			invoice.setInvoiceNumber(invoiceNumber);

			// Save the invoice
			Invoice savedInvoice = invoiceService.generateInvoice(invoice);

			treatment.setInvoiceGenerated(true);
			treatment.setTreatmentStatus(TreatmentStatus.BILLED);

			Treatment updatedTreatment = treatmentService.updateTreatmentDetails(treatment);
			logger.debug("updatedTreatment: {}", updatedTreatment);

			// Should Prescription Status be updated??!?!?

			model.addAttribute("source", source);

			// Set success message and redirect to invoice details page
			redirectAttributes.addFlashAttribute("message", MessageConstants.INVOICE_CREATED_SUCCESS);
			return "redirect:/invoices/viewinvoice?invoiceID=" + savedInvoice.getInvoiceID() + "&source=" + source;

		} catch (Exception e) {
			// Handle exceptions
			logger.error("{}: {}", MessageConstants.INVOICE_CREATED_FAILURE, e.getLocalizedMessage());
			redirectAttributes.addFlashAttribute("errorMessage", MessageConstants.INVOICE_CREATED_FAILURE);

			// Repopulate the form data in case of error
			Optional<Treatment> treatmentOptional = treatmentService.getTreatmentDetails(treatmentID);
			Treatment treatment = new Treatment();

			if (treatmentOptional.isPresent()) {
				treatment = treatmentOptional.get();
				logger.debug("treatment: {}", treatment);
				model.addAttribute("treatment", treatment);
			}

			Optional<Prescription> prescriptionOptional = null;
			Prescription prescription = new Prescription();

			if (prescriptionID != null && prescriptionID > 0) {
				prescriptionOptional = prescriptionService.getPrescriptionDetails(prescriptionID);
				if (prescriptionOptional.isPresent()) {
					prescription = prescriptionOptional.get();
					logger.debug("prescription: {}", prescription);
					model.addAttribute("prescription", prescription);
				}
			}

			return "redirect:/invoices/generate-invoice?treatmentID=" + treatmentID;
		}

	}

	@GetMapping("/viewinvoice")
	public String viewInvoice(@RequestParam(RequestParamConstants.INVOICE_ID) Long invoiceID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Loading the View Invoice Page!!");

		try {

			Optional<Invoice> invoiceOptional = invoiceService.getInvoiceDetails(invoiceID);
			Invoice invoice = new Invoice();

			if (invoiceOptional.isPresent()) {

				String year = String.valueOf(java.time.Year.now());
				String invoiceNumber = "#INV-" + year + "-" + invoiceID;
				String currentDate = java.time.LocalDateTime.now().toString().replace("T", " ");

				invoice = invoiceOptional.get();
				logger.debug("invoice: {}", invoice);
				model.addAttribute("invoice", invoice);

				String formattedInvoiceDate = DateFormatter.formatWithOrdinalSuffix(invoice.getInvoiceDate());
				String formattedTreatmentDate = null;

				if (invoice.getTreatment() != null) {

					Treatment treatment = invoice.getTreatment();

					LocalDate treatmentDate = treatment.getTreatmentDate();
					LocalDateTime treatmentDateTime = treatmentDate.atStartOfDay();
					formattedTreatmentDate = DateFormatter.formatWithOrdinalSuffix(treatmentDateTime);

					Optional<Doctor> doctorOptional = doctorService.getDoctorDetails(treatment.getDoctorID());
					Doctor doctor = new Doctor();
					if (doctorOptional.isPresent()) {
						doctor = doctorOptional.get();
						logger.debug("doctor: {}", doctor);
						model.addAttribute("doctor", doctor);
					}

					Optional<Patient> patientOptional = patientService.getPatientDetails(treatment.getPatientID());
					Patient patient = new Patient();
					if (patientOptional.isPresent()) {
						patient = patientOptional.get();
						logger.debug("patient: {}", patient);
						model.addAttribute("patient", patient);

						User user = patient.getUser();
						logger.debug("user: {}", user);
						model.addAttribute("user", user);
					}
				}

				model.addAttribute("invoiceNumber", invoiceNumber);
				model.addAttribute("invoiceDate", currentDate);
				model.addAttribute("formattedInvoiceDate", formattedInvoiceDate);
				model.addAttribute("formattedTreatmentDate", formattedTreatmentDate);

			}

		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.INVOICE_LOAD_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.INVOICE_LOAD_ERROR);
			return source;
		}

		model.addAttribute("source", source);

		return "viewinvoice";
	}

	@GetMapping("/update-invoice")
	public String updateInvoice(@RequestParam(RequestParamConstants.INVOICE_ID) Long invoiceID,
			@RequestParam(RequestParamConstants.INVOICE_STATUS) String invoiceStatus,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model,
			RedirectAttributes redirectAttributes) {
		logger.info("Loading the View Invoice after Payment!!");

		if (logger.isDebugEnabled()) {
			logger.debug("Request parameters - invoiceID: {},  invoiceStatus: {}, source: {}", invoiceID, invoiceStatus,
					source);
		}

		String treatmentID = null;
		try {

			Optional<Invoice> invoiceOptional = invoiceService.getInvoiceDetails(invoiceID);
			Invoice invoice = new Invoice();

			if (invoiceOptional.isPresent()) {

				invoice = invoiceOptional.get();
				if (invoiceStatus.equals(InvoiceStatus.PAID.name()))
					invoice.setInvoiceStatus(InvoiceStatus.PAID);
				else if (invoiceStatus.equals(InvoiceStatus.CANCELLED.name()))
					invoice.setInvoiceStatus(InvoiceStatus.CANCELLED);

				invoiceService.updateInvoiceStatus(invoice);

				if (invoice.getTreatment() != null) {
					Treatment treatment = invoice.getTreatment();
					treatmentID = treatment.getTreatmentID().toString();
				}
			}

		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.INVOICE_STATUS_UPDATE_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.INVOICE_STATUS_UPDATE_ERROR);
			return source;
		}

		model.addAttribute("source", source);

		if (invoiceStatus.equals(InvoiceStatus.PAID.name()))
			return "redirect:/invoices/viewinvoice?invoiceID=" + invoiceID + "&source=" + source;
		else if (invoiceStatus.equals(InvoiceStatus.CANCELLED.name())) {
			redirectAttributes.addAttribute("treatmentID", treatmentID);
			redirectAttributes.addAttribute("source", source);
			return "redirect:/invoices/generate-invoice";
		}
		return "viewinvoice";

	}

}