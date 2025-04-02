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

import com.healthcaremngnt.model.Invoice;

@Component
@StepScope
public class FinanceReportItemWriter implements ItemWriter<Invoice> {

	private static final Logger logger = LogManager.getLogger(FinanceReportItemWriter.class);

	private boolean reportGenerated = false;
	private int totalItemsWritten = 0;

	private LocalDateTime now = LocalDateTime.now();
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
	private String formattedNow = now.format(formatter);
	private String directory = "reports";

	private LocalDate today = LocalDate.now();

	@Override
	public void write(Chunk<? extends Invoice> chunk) throws Exception {

		logger.info("FinanceReportItemWriter::: write() with {} items", chunk.getItems().size());
		totalItemsWritten += chunk.getItems().size();
		logger.info("Total items written so far: {}", totalItemsWritten);

		if (chunk == null || chunk.getItems().isEmpty()) {
			logger.info("No invoices found to generate report");
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

	private void generateReport(List<? extends Invoice> invoices) throws IOException {
		logger.info("FinanceReportItemWriter::: generateReport()");

		logger.debug("now: {}", now);
		logger.debug("formattedNow: {}", formattedNow);

		// Ensure the directory exists
		Files.createDirectories(Paths.get(directory));

		// Generate text report
		String txtFilename = directory + "/finance/finance_report_" + formattedNow + ".txt";
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFilename, true))) {
			writer.write("Finance Report generated on::: " + today + "\n\n");
			for (Invoice invoice : invoices) {
//				writer.write("Patient: " + invoice.getPatient().getPatientName() + "\n");
//				writer.write("Doctor: " + invoice.getDoctor().getDoctorName() + "\n");
//				writer.write("Date: " + invoice.getAppointmentDate() + "\n");
//				writer.write("Time: " + invoice.getAppointmentTime() + "\n");
//				writer.write("Priority: " + invoice.getPriority() + "\n");
//				writer.write("Needs Reminder: " + invoice.isNeedsReminder() + "\n");
//				writer.write("Processed: " + invoice.isProcessed() + "\n");
//				writer.write("\n");
			}
		}
		logger.info("Text report generated successfully: " + txtFilename);

		// Generate PDF report
		String pdfFilename = directory + "/finance/fiance_report_" + formattedNow + ".pdf";
		try (PDDocument document = new PDDocument()) {
			PDPage page = new PDPage();
			document.addPage(page);

			try (PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(0),
					PDPageContentStream.AppendMode.APPEND, true, true)) {
				contentStream.beginText();
				contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
				contentStream.setLeading(14.5f);
				contentStream.newLineAtOffset(25, 700);
				contentStream.showText("Finance Report generated on::: " + today);
				contentStream.newLine();
				contentStream.newLine();

				for (Invoice invoice : invoices) {
					contentStream.setFont(PDType1Font.HELVETICA, 12);
//					contentStream.showText("Patient: " + invoice.getPatient().getPatientName());
//					contentStream.newLine();
//					contentStream.showText("Doctor: " + invoice.getDoctor().getDoctorName());
//					contentStream.newLine();
//					contentStream.showText("Date: " + invoice.getAppointmentDate());
//					contentStream.newLine();
//					contentStream.showText("Time: " + invoice.getAppointmentTime());
//					contentStream.newLine();
//					contentStream.showText("Priority: " + invoice.getPriority());
//					contentStream.newLine();
//					contentStream.showText("Needs Reminder: " + invoice.isNeedsReminder());
//					contentStream.newLine();
//					contentStream.showText("Processed: " + invoice.isProcessed());
					contentStream.newLine();
					contentStream.newLine();
				}
				contentStream.endText();
			}
			document.save(pdfFilename);
		}
		logger.info("PDF report generated successfully: " + pdfFilename);

		// Generate Word report
		String wordFilename = directory + "/finance/finance_report_" + formattedNow + ".docx";

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
		run.setBold(true);
		run.setText("Finance Report generated on::: " + today);
		run.addBreak();
		run.addBreak();

		for (Invoice invoice : invoices) {
			run = paragraph.createRun();
//			run.setText("Patient: " + invoice.getPatient().getPatientName());
//			run.addBreak();
//			run.setText("Doctor: " + invoice.getDoctor().getDoctorName());
//			run.addBreak();
//			run.setText("Date: " + invoice.getAppointmentDate());
//			run.addBreak();
//			run.setText("Time: " + invoice.getAppointmentTime());
//			run.addBreak();
//			run.setText("Priority: " + invoice.getPriority());
//			run.addBreak();
//			run.setText("Needs Reminder: " + invoice.isNeedsReminder());
//			run.addBreak();
//			run.setText("Processed: " + invoice.isProcessed());
			run.addBreak();
			run.addBreak();
		}
		try (FileOutputStream out = new FileOutputStream(wordFilename)) {
			document.write(out);
		}

		logger.info("Word report generated successfully: " + wordFilename);

		// Generate Excel report
		String excelFilename = directory + "/finance/finance_report_" + formattedNow + ".xlsx";
		Workbook workbook;
		Sheet sheet;
		int rowNum;

		File excelFile = new File(excelFilename);
		if (excelFile.exists()) {
			try (FileInputStream fis = new FileInputStream(excelFile)) {
				workbook = new XSSFWorkbook(fis);
			}
			sheet = workbook.getSheet("Invoices");
			rowNum = sheet.getLastRowNum() + 1;
		} else {
			workbook = new XSSFWorkbook();
			sheet = workbook.createSheet("Invoices");

			// Create header row
			Row headerRow = sheet.createRow(0);
			Cell headerCell;
			String[] headers = { "Patient", "Doctor", "Date", "Time", "Priority", "Needs Reminder", "Processed" };
			for (int i = 0; i < headers.length; i++) {
				headerCell = headerRow.createCell(i);
				headerCell.setCellValue(headers[i]);
			}
			rowNum = 1;
		}

		// Create data rows
		for (Invoice invoice : invoices) {
			Row row = sheet.createRow(rowNum++);
//			row.createCell(0).setCellValue(invoice.getPatient().getPatientName());
//			row.createCell(1).setCellValue(invoice.getDoctor().getDoctorName());
//			row.createCell(2).setCellValue(invoice.getAppointmentDate().toString());
//			row.createCell(3).setCellValue(invoice.getAppointmentTime().toString());
//			row.createCell(4).setCellValue(invoice.getPriority().name());
//			row.createCell(5).setCellValue(invoice.isNeedsReminder());
//			row.createCell(6).setCellValue(invoice.isProcessed());
		}

		try (FileOutputStream fileOut = new FileOutputStream(excelFilename)) {
			workbook.write(fileOut);
		}

		logger.info("Excel report generated successfully: " + excelFilename);
	}

}
