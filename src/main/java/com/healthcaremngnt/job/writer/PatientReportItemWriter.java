package com.healthcaremngnt.job.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.healthcaremngnt.enums.Salutation;
import com.healthcaremngnt.model.Patient;

@Component
@StepScope
public class PatientReportItemWriter implements ItemWriter<Patient> {

	private static final Logger logger = LogManager.getLogger(PatientReportItemWriter.class);

	private boolean reportGenerated = false;
	private int totalItemsWritten = 0;

	private LocalDateTime now = LocalDateTime.now();
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
	private String formattedNow = now.format(formatter);
	private String directory = "reports";

	private LocalDate today = LocalDate.now();

	@Override
	public void write(Chunk<? extends Patient> chunk) throws Exception {

		logger.info("PatientReportItemWriter::: write() with {} items", chunk.getItems().size());
		totalItemsWritten += chunk.getItems().size();
		logger.info("Total items written so far: {}", totalItemsWritten);

		if (chunk == null || chunk.getItems().isEmpty()) {
			logger.info("No patients found to generate report");
			reportGenerated = false;
			return;
		}

		generateReport(chunk.getItems());
		reportGenerated = true;
	}

	public boolean isReportGenerated() {
		return reportGenerated;
	}

	public void resetState() {
		reportGenerated = false;
		totalItemsWritten = 0;
	}

	@SuppressWarnings("resource")
	private void generateReport(List<? extends Patient> patients) throws IOException {
		logger.info("PatientReportItemWriter::: generateReport()");

		logger.debug("now: {}", now);
		logger.debug("formattedNow: {}", formattedNow);

		// Ensure the directory exists
		Files.createDirectories(Paths.get(directory));

		// Generate text report
		String txtFilename = directory + "/patient/patient_report_" + formattedNow + ".txt";
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFilename, true))) {
			writer.write("Patient Report generated on::: " + today + "\n\n");
			for (Patient patient : patients) {
				String salutation = (patient.getSalutation() == Salutation.CUSTOM) ? patient.getCustomSalutation()
						: patient.getSalutation().name();

				writer.write("Patient ID: " + patient.getPatientID() + "\n");
				writer.write("User ID: " + patient.getUser().getUserID() + "\n");
				writer.write("User Name: " + patient.getUser().getUserName() + "\n");
				writer.write("Patient Name: " + salutation + " " + patient.getPatientName() + "\n");
				writer.write("Gender: " + patient.getGender() + "\n");
				writer.write("Date of Birth: " + patient.getDob() + "\n");
				writer.write("Address: " + patient.getAddress() + "\n");
				writer.write("Contact Number: " + patient.getContactNumber() + "\n");
				writer.write("Email ID: " + patient.getUser().getEmailID() + "\n");
				writer.write("\n");
			}
		}
		logger.info("Text report generated successfully: " + txtFilename);

		// Generate PDF report --> working code but only one page will be generated!!!
//		String pdfFilename = directory + "/patient/patient_report_" + formattedNow + ".pdf";
//		try (PDDocument document = new PDDocument()) {
//			PDPage page = new PDPage();
//			document.addPage(page);
//
//			try (PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(0),
//					PDPageContentStream.AppendMode.APPEND, true, true)) {
//				contentStream.beginText();
//				contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
//				contentStream.setLeading(14.5f);
//				contentStream.newLineAtOffset(25, 700);
//				contentStream.showText("Patient Report generated on::: " + today);
//				contentStream.newLine();
//				contentStream.newLine();
//
//				for (Patient patient : patients) {
//					contentStream.setFont(PDType1Font.HELVETICA, 12);
//					contentStream.showText("Patient ID: " + patient.getPatientName());
//					contentStream.newLine();
//					contentStream.showText("Patient Name: " + patient.getPatientName());
//					contentStream.newLine();
//					//
//					contentStream.newLine();
//					contentStream.newLine();
//				}
//				contentStream.endText();
//			}
//			document.save(pdfFilename);
//		}
//		logger.info("PDF report generated successfully: " + pdfFilename);

		// Generate PDF report
		String pdfFilename = directory + "/patient/patient_report_" + formattedNow + ".pdf";

		File file = new File(pdfFilename);
		boolean appendMode = file.exists(); // Check if the file exists

		try (PDDocument pdfDocument = appendMode ? PDDocument.load(file) : new PDDocument()) {
			PDPage page;
			PDPageContentStream contentStream;

			if (appendMode) {
				// Open existing PDF and get last page
				page = pdfDocument.getPage(pdfDocument.getNumberOfPages() - 1);
				contentStream = new PDPageContentStream(pdfDocument, page, PDPageContentStream.AppendMode.APPEND, true,
						true);
			} else {
				// Create a new PDF and first page
				page = new PDPage();
				pdfDocument.addPage(page);
				contentStream = new PDPageContentStream(pdfDocument, page, PDPageContentStream.AppendMode.APPEND, true,
						true);

				// Write header only once
				contentStream.beginText();
				contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
				contentStream.newLineAtOffset(25, 700);
				contentStream.showText("Patient Report generated on: " + today);
				contentStream.newLine();
				contentStream.newLine();
				contentStream.endText();
			}

			float yPosition = findLastYPosition(page); // Ensure new records start below previous content
			contentStream.beginText();
			contentStream.setFont(PDType1Font.HELVETICA, 12);
			contentStream.newLineAtOffset(25, yPosition);

			// Process each patient
			for (Patient patient : patients) {

				String salutation = (patient.getSalutation() == Salutation.CUSTOM) ? patient.getCustomSalutation()
						: patient.getSalutation().name();

				if (yPosition < 50 + (7 * 14.5f)) { // Check if we need a new page
					contentStream.endText();
					contentStream.close();

					page = new PDPage();
					pdfDocument.addPage(page);
					contentStream = new PDPageContentStream(pdfDocument, page, PDPageContentStream.AppendMode.APPEND,
							true, true);
					yPosition = 700;

					contentStream.beginText();
					contentStream.setFont(PDType1Font.HELVETICA, 12);
					contentStream.newLineAtOffset(25, yPosition);
				}

				// Write patient details correctly
				contentStream.newLine();
				contentStream.showText("Patient ID: " + patient.getPatientID());
				contentStream.newLine();
				contentStream.showText("User ID: " + patient.getUser().getUserID());
				contentStream.newLine();
				contentStream.showText("User Name: " + patient.getUser().getUserName());
				contentStream.newLine();
				contentStream.showText("Patient Name: " + salutation + " " + patient.getPatientName());
				contentStream.newLine();
				contentStream.showText("Gender: " + patient.getGender());
				contentStream.newLine();
				contentStream.showText("Date of Birth: " + patient.getDob());
				contentStream.newLine();
				contentStream.showText("Address: " + patient.getAddress());
				contentStream.newLine();
				contentStream.showText("Contact Number: " + patient.getContactNumber());
				contentStream.newLine();
				contentStream.showText("Email ID: " + patient.getUser().getEmailID());
				contentStream.newLine();
				contentStream.showText("--------------------------------------------"); // Separator
				contentStream.newLine();

				logger.debug(
						"Processing patient: " + patient.getPatientID() + ", Patient: " + patient.getPatientName());

				yPosition -= (9 * 14.5f); // Adjust downward after writing each patient
			}
			contentStream.endText();
			contentStream.close();
			pdfDocument.save(pdfFilename);

		}

		logger.info("PDF report generated successfully: " + pdfFilename);

		// Generate Word report
		String wordFilename = directory + "/patient/patient_report_" + formattedNow + ".docx";

		XWPFDocument document;
		File wordFile = new File(wordFilename);
		if (wordFile.exists()) {
			try (FileInputStream fis = new FileInputStream(wordFile)) {
				document = new XWPFDocument(fis);
			}
		} else {
			document = new XWPFDocument();
		}

		XWPFParagraph paragraph = document.createParagraph();
		XWPFRun run = paragraph.createRun();

		if (!wordFile.exists()) {
			run.setBold(true);
			run.setText("Patient Report generated on::: " + today);
			run.addBreak();
			run.addBreak();
		}

		for (Patient patient : patients) {

			String salutation = (patient.getSalutation() == Salutation.CUSTOM) ? patient.getCustomSalutation()
					: patient.getSalutation().name();

			run = paragraph.createRun();
			run.setText("Patient ID: " + patient.getPatientID());
			run.addBreak();
			run.setText("User ID: " + patient.getUser().getUserID());
			run.addBreak();
			run.setText("User Name: " + patient.getUser().getUserName());
			run.addBreak();
			run.setText("Patient Name: " + salutation + " " + patient.getPatientName());
			run.addBreak();
			run.setText("Gender: " + patient.getGender());
			run.addBreak();
			run.setText("Date of Birth: " + patient.getDob());
			run.addBreak();
			run.setText("Address: " + patient.getAddress());
			run.addBreak();
			run.setText("Contact Number: " + patient.getContactNumber());
			run.addBreak();
			run.setText("Email ID: " + patient.getUser().getEmailID() + "\n");
			run.addBreak();
			run.addBreak();
		}
		try (FileOutputStream out = new FileOutputStream(wordFilename)) {
			document.write(out);
		}

		logger.info("Word report generated successfully: " + wordFilename);

		// Generate Excel report
		String excelFilename = directory + "/patient/patient_report_" + formattedNow + ".xlsx";
		Workbook workbook;
		Sheet sheet;
		int rowNum;

		File excelFile = new File(excelFilename);
		if (excelFile.exists()) {
			try (FileInputStream fis = new FileInputStream(excelFile)) {
				workbook = new XSSFWorkbook(fis);
			}
			sheet = workbook.getSheet("Patients");
			rowNum = sheet.getLastRowNum() + 1;
		} else {
			workbook = new XSSFWorkbook();
			sheet = workbook.createSheet("Patients");

			// Create header row
			Row headerRow = sheet.createRow(0);
			Cell headerCell;
			String[] headers = { "Patient ID", "User ID", "User Name", "Patient Name", "Gender", "Date of Birth",
					"Address", "Contact Number", "Email ID" };
			for (int i = 0; i < headers.length; i++) {
				headerCell = headerRow.createCell(i);
				headerCell.setCellValue(headers[i]);
			}
			rowNum = 1;
		}

		// Create data rows
		for (Patient patient : patients) {
			String salutation = (patient.getSalutation() == Salutation.CUSTOM) ? patient.getCustomSalutation()
					: patient.getSalutation().name();

			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(patient.getPatientID() + "\n");
			row.createCell(1).setCellValue(patient.getUser().getUserID() + "\n");
			row.createCell(2).setCellValue(patient.getUser().getUserName() + "\n");
			row.createCell(3).setCellValue(salutation + " " + patient.getPatientName() + "\n");
			row.createCell(4).setCellValue(patient.getGender() + "\n");
			row.createCell(5).setCellValue(patient.getDob() + "\n");
			row.createCell(6).setCellValue(patient.getAddress() + "\n");
			row.createCell(7).setCellValue(patient.getContactNumber() + "\n");
			row.createCell(8).setCellValue(patient.getUser().getEmailID() + "\n");
		}

		try (FileOutputStream fileOut = new FileOutputStream(excelFilename)) {
			workbook.write(fileOut);
		}

		logger.info("Excel report generated successfully: " + excelFilename);
	}

	private float findLastYPosition(PDPage page) {
		// Logic to estimate last used Y position (you can fine-tune based on text size)
		return page.getMediaBox().getHeight() - 50; // Approximate bottom margin for continuing text
	}

}
