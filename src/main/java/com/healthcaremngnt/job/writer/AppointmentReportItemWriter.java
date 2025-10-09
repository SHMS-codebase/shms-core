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
import org.apache.pdfbox.text.PDFTextStripper;
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

import com.healthcaremngnt.model.Appointment;

@Component
@StepScope
public class AppointmentReportItemWriter implements ItemWriter<Appointment> {

	private static final Logger logger = LogManager.getLogger(AppointmentReportItemWriter.class);

	private boolean reportGenerated = false;
	private int totalItemsWritten = 0;

	private LocalDateTime now = LocalDateTime.now();
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
	private String formattedNow = now.format(formatter);
	private String directory = "reports";

	private LocalDate today = LocalDate.now();

	@Override
	public void write(Chunk<? extends Appointment> chunk) throws Exception {

		logger.info("AppointmentReportItemWriter::: write() with {} items", chunk.getItems().size());
		totalItemsWritten += chunk.getItems().size();
		logger.info("Total items written so far: {}", totalItemsWritten);

		if (chunk == null || chunk.getItems().isEmpty()) {
			logger.info("No appointments found to generate report");
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
	private void generateReport(List<? extends Appointment> appointments) throws IOException {
		logger.info("AppointmentReportItemWriter::: generateReport()");

		logger.debug("now: {}", now);
		logger.debug("formattedNow: {}", formattedNow);

		// Ensure the directory exists
		Files.createDirectories(Paths.get(directory));

		// Generate text report
		String txtFilename = directory + "/appointment/appointment_report_" + formattedNow + ".txt";
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFilename, true))) {
			writer.write("Appointment Report generated on::: " + today + "\n\n");
			for (Appointment appointment : appointments) {
				writer.write("Appointment ID: " + appointment.getAppointmentID() + "\n");
				writer.write("Appointment Date: " + appointment.getAppointmentDate() + "\n");
				writer.write("Appointment Time: " + appointment.getAppointmentTime() + "\n");
				writer.write("Patient: " + appointment.getPatient().getPatientName() + "\n");
				writer.write("Doctor: " + appointment.getDoctor().getDoctorName() + "\n");
				writer.write("Appointment Status: " + appointment.getAppointmentStatus() + "\n");
				writer.write("Priority: " + appointment.getPriority() + "\n");
				writer.write("Needs Reminder: " + appointment.getNeedsReminder() + "\n");
				writer.write("\n");
			}
		}
		logger.info("Text report generated successfully: " + txtFilename);

		// Generate PDF report
		String pdfFilename = directory + "/appointment/appointment_report_" + formattedNow + ".pdf";

		File file = new File(pdfFilename);
		boolean appendMode = file.exists();

		try (PDDocument pdfDocument = appendMode ? PDDocument.load(file) : new PDDocument()) {
		    PDPage page;
		    PDPageContentStream contentStream;
		    float yPosition;
		    float lineHeight = 15f;
		    float margin = 25;
		    float pageHeight = 700;
		    float bottomMargin = 50;

		    if (appendMode) {
		        page = pdfDocument.getPage(pdfDocument.getNumberOfPages() - 1);
		        yPosition = findLastYPosition(page) - (lineHeight * 2);
		        contentStream = new PDPageContentStream(pdfDocument, page, 
		            PDPageContentStream.AppendMode.APPEND, true, true);
		    } else {
		        page = new PDPage();
		        pdfDocument.addPage(page);
		        yPosition = pageHeight;
		        contentStream = new PDPageContentStream(pdfDocument, page);
		        
		        // Write header
		        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
		        contentStream.beginText();
		        contentStream.newLineAtOffset(margin, yPosition);
		        contentStream.showText("Appointment Report generated on: " + today);
		        contentStream.endText();
		        
		        yPosition -= (lineHeight * 3);
		    }

		    contentStream.setFont(PDType1Font.HELVETICA, 12);

		    // Process each appointment
		    for (Appointment appointment : appointments) {
		        // Check if we need a new page (need space for 10 lines)
		        if (yPosition < bottomMargin + (10 * lineHeight)) {
		            contentStream.close();
		            
		            page = new PDPage();
		            pdfDocument.addPage(page);
		            yPosition = pageHeight;
		            contentStream = new PDPageContentStream(pdfDocument, page);
		            contentStream.setFont(PDType1Font.HELVETICA, 12);
		        }

		        // Write each line separately with proper Y positioning
		        contentStream.beginText();
		        contentStream.newLineAtOffset(margin, yPosition);
		        contentStream.showText("Appointment ID: " + appointment.getAppointmentID());
		        contentStream.endText();
		        yPosition -= lineHeight;

		        contentStream.beginText();
		        contentStream.newLineAtOffset(margin, yPosition);
		        contentStream.showText("Appointment Date: " + appointment.getAppointmentDate());
		        contentStream.endText();
		        yPosition -= lineHeight;

		        contentStream.beginText();
		        contentStream.newLineAtOffset(margin, yPosition);
		        contentStream.showText("Appointment Time: " + appointment.getAppointmentTime());
		        contentStream.endText();
		        yPosition -= lineHeight;

		        contentStream.beginText();
		        contentStream.newLineAtOffset(margin, yPosition);
		        contentStream.showText("Patient: " + appointment.getPatient().getPatientName());
		        contentStream.endText();
		        yPosition -= lineHeight;

		        contentStream.beginText();
		        contentStream.newLineAtOffset(margin, yPosition);
		        contentStream.showText("Doctor: " + appointment.getDoctor().getDoctorName());
		        contentStream.endText();
		        yPosition -= lineHeight;

		        contentStream.beginText();
		        contentStream.newLineAtOffset(margin, yPosition);
		        contentStream.showText("Appointment Status: " + appointment.getAppointmentStatus());
		        contentStream.endText();
		        yPosition -= lineHeight;

		        contentStream.beginText();
		        contentStream.newLineAtOffset(margin, yPosition);
		        contentStream.showText("Priority: " + appointment.getPriority());
		        contentStream.endText();
		        yPosition -= lineHeight;

		        contentStream.beginText();
		        contentStream.newLineAtOffset(margin, yPosition);
		        contentStream.showText("Needs Reminder: " + appointment.getNeedsReminder());
		        contentStream.endText();
		        yPosition -= lineHeight;

		        contentStream.beginText();
		        contentStream.newLineAtOffset(margin, yPosition);
		        contentStream.showText("--------------------------------------------");
		        contentStream.endText();
		        yPosition -= lineHeight;
		        
		        // Extra space between appointments
		        yPosition -= lineHeight;

		        logger.debug("Processing appointment: " + appointment.getAppointmentID());
		    }

		    contentStream.close();
		    pdfDocument.save(pdfFilename);
		}

		logger.info("PDF report generated successfully: " + pdfFilename);

		// Now extract and log the content
		try (PDDocument savedDoc = PDDocument.load(new File(pdfFilename))) {
			PDFTextStripper stripper = new PDFTextStripper();
			String text = stripper.getText(savedDoc);
			logger.info("PDF Content: \n" + text);
		} catch (IOException e) {
			logger.error("Error extracting PDF content for logging", e);
		}

		// Generate Word report
		String wordFilename = directory + "/appointment/appointment_report_" + formattedNow + ".docx";

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
			run.setText("Appointment Report generated on::: " + today);
			run.addBreak();
			run.addBreak();
		}		

		for (Appointment appointment : appointments) {
			run = paragraph.createRun();
			
			run.setText("Appointment ID: " + appointment.getAppointmentID());
			run.addBreak();
			run.setText("Appointment Date: " + appointment.getAppointmentDate());
			run.addBreak();
			run.setText("Appointment Time: " + appointment.getAppointmentTime());
			run.addBreak();
			run.setText("Patient: " + appointment.getPatient().getPatientName());
			run.addBreak();
			run.setText("Doctor: " + appointment.getDoctor().getDoctorName());
			run.addBreak();
			run.setText("Appointment Status: " + appointment.getAppointmentStatus());
			run.addBreak();
			run.setText("Priority: " + appointment.getPriority());
			run.addBreak();
			run.setText("Needs Reminder: " + appointment.getNeedsReminder());
			run.addBreak();
			run.addBreak();
		}
		try (FileOutputStream out = new FileOutputStream(wordFilename)) {
			document.write(out);
		}

		logger.info("Word report generated successfully: " + wordFilename);

		// Generate Excel report
		String excelFilename = directory + "/appointment/appointment_report_" + formattedNow + ".xlsx";
		Workbook workbook;
		Sheet sheet;
		int rowNum;

		File excelFile = new File(excelFilename);
		if (excelFile.exists()) {
			try (FileInputStream fis = new FileInputStream(excelFile)) {
				workbook = new XSSFWorkbook(fis);
			}
			sheet = workbook.getSheet("Appointments");
			rowNum = sheet.getLastRowNum() + 1;
		} else {
			workbook = new XSSFWorkbook();
			sheet = workbook.createSheet("Appointments");

			// Create header row
			Row headerRow = sheet.createRow(0);
			Cell headerCell;
			String[] headers = { "Appointment ID", "Appointment Date", "Appointment Time", "Patient", "Doctor", "Appointment Status", "Priority", "Needs Reminder" };
			for (int i = 0; i < headers.length; i++) {
				headerCell = headerRow.createCell(i);
				headerCell.setCellValue(headers[i]);
			}
			rowNum = 1;
		}

		// Create data rows
		for (Appointment appointment : appointments) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(appointment.getAppointmentID().toString());
			row.createCell(1).setCellValue(appointment.getAppointmentDate().toString());
			row.createCell(2).setCellValue(appointment.getAppointmentTime().toString());
			row.createCell(3).setCellValue(appointment.getPatient().getPatientName());
			row.createCell(4).setCellValue(appointment.getDoctor().getDoctorName());
			row.createCell(5).setCellValue(appointment.getAppointmentStatus().name());
			row.createCell(6).setCellValue(appointment.getPriority().name());
			row.createCell(7).setCellValue(appointment.getNeedsReminder());
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
