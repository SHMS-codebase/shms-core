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

import com.healthcaremngnt.enums.Salutation;
import com.healthcaremngnt.model.Invoice;
import com.healthcaremngnt.model.Patient;

@Component
@StepScope
public class BillingReportItemWriter implements ItemWriter<Invoice> {

	private static final Logger logger = LogManager.getLogger(BillingReportItemWriter.class);

	private boolean reportGenerated = false;
	private int totalItemsWritten = 0;

	private LocalDateTime now = LocalDateTime.now();
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
	private String formattedNow = now.format(formatter);
	private String directory = "reports";

	private LocalDate today = LocalDate.now();

	@Override
	public void write(Chunk<? extends Invoice> chunk) throws Exception {

		logger.info("BillingReportItemWriter::: write() with {} items", chunk.getItems().size());
		totalItemsWritten += chunk.getItems().size();
		logger.info("Total items written so far: {}", totalItemsWritten);

		if (chunk == null || chunk.getItems().isEmpty()) {
			logger.info("No billing info found to generate report");
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
	private void generateReport(List<? extends Invoice> invoices) throws IOException {
		logger.info("BillingReportItemWriter::: generateReport()");

		logger.debug("now: {}", now);
		logger.debug("formattedNow: {}", formattedNow);

		// Ensure the directory exists
		Files.createDirectories(Paths.get(directory));

		// Generate text report
		String txtFilename = directory + "/billing/billing_report_" + formattedNow + ".txt";
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFilename, true))) {
			writer.write("Billing Report generated on::: " + today + "\n\n");
			for (Invoice invoice : invoices) {
				Patient patient = invoice.getTreatment().getAppointment().getPatient();
				String salutation = (patient.getSalutation() == Salutation.CUSTOM) ? patient.getCustomSalutation()
						: patient.getSalutation().name();

				writer.write("Invoice ID: " + invoice.getInvoiceID() + "\n");
				writer.write("Invoice Date: " + invoice.getInvoiceDate() + "\n");
				writer.write("Patient: " + salutation + " " + patient.getPatientName() + "\n");
				writer.write("Treatment Cost: ₹" + invoice.getTreatmentCost() + "\n");
				writer.write("Prescription Cost: ₹" + invoice.getPrescriptionCost() + "\n");
				writer.write("Total Cost: ₹" + invoice.getTotalAmount() + "\n");
				writer.write("Invoice Status: " + invoice.getInvoiceStatus() + "\n");
				writer.write("\n");
			}
		}
		logger.info("Text report generated successfully: " + txtFilename);

		// Generate PDF report
		String pdfFilename = directory + "/billing/billing_report_" + formattedNow + ".pdf";

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
				contentStream.showText("Billing Report generated on: " + today);
				contentStream.newLine();
				contentStream.newLine();
				contentStream.endText();
			}

			float yPosition = findLastYPosition(page); // Ensure new records start below previous content
			contentStream.beginText();
			contentStream.setFont(PDType1Font.HELVETICA, 12);
			contentStream.newLineAtOffset(25, yPosition);

			// Process each invoice
			for (Invoice invoice : invoices) {
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

				// Write invoice details correctly
				contentStream.newLine();
				contentStream.showText("Invoice ID: " + invoice.getInvoiceID());
				contentStream.newLine();
				contentStream.showText("Invoice Date: " + invoice.getInvoiceDate());
				contentStream.newLine();
				Patient patient = invoice.getTreatment().getAppointment().getPatient();
				String salutation = (patient.getSalutation() == Salutation.CUSTOM) ? patient.getCustomSalutation()
						: patient.getSalutation().name();

				contentStream.showText("Patient: " + salutation + " " + patient.getPatientName());
				contentStream.newLine();
				contentStream.showText("Treatment Cost: " + invoice.getTreatmentCost());
				contentStream.newLine();
				contentStream.showText("Prescription Cost: " + invoice.getPrescriptionCost());
				contentStream.newLine();
				contentStream.showText("Total Cost: " + invoice.getTotalAmount());
				contentStream.newLine();
				contentStream.showText("Invoice Status: " + invoice.getInvoiceStatus());
				contentStream.newLine();
				contentStream.showText("--------------------------------------------"); // Separator
				contentStream.newLine();

				logger.debug("Processing invoice: " + invoice.getInvoiceID() + ", Patient: "
						+ invoice.getTreatment().getAppointment().getPatient().getPatientName());

				yPosition -= (9 * 14.5f); // Adjust downward after writing each invoice
			}
			contentStream.endText();
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
		String wordFilename = directory + "/billing/billing_report_" + formattedNow + ".docx";

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
			run.setText("Billing Report generated on::: " + today);
			run.addBreak();
			run.addBreak();
		}

		for (Invoice invoice : invoices) {

			Patient patient = invoice.getTreatment().getAppointment().getPatient();
			String salutation = (patient.getSalutation() == Salutation.CUSTOM) ? patient.getCustomSalutation()
					: patient.getSalutation().name();

			run = paragraph.createRun();
			run.setText("Invoice ID: " + invoice.getInvoiceID());
			run.addBreak();
			run.setText("Invoice Date: " + invoice.getInvoiceDate());
			run.addBreak();
			run.setText("Patient: " + salutation + " " + patient.getPatientName());
			run.addBreak();
			run.setText("Treatment Cost: ₹" + invoice.getTreatmentCost());
			run.addBreak();
			run.setText("Prescription Cost: ₹" + invoice.getPrescriptionCost());
			run.addBreak();
			run.setText("Total Cost: ₹" + invoice.getTotalAmount());
			run.addBreak();
			run.setText("Invoice Status: " + invoice.getInvoiceStatus());
			run.addBreak();
			run.addBreak();
		}

		try (FileOutputStream out = new FileOutputStream(wordFilename)) {
			document.write(out);
		}

		logger.info("Word report generated successfully: " + wordFilename);

		// Generate Excel report
		String excelFilename = directory + "/billing/billing_report_" + formattedNow + ".xlsx";
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
			String[] headers = { "Invoice ID", "Invoice Date", "Patient", "Treatment Cost (₹)", "Prescription Cost (₹)",
					"Total Cost (₹)", "Invoice Status" };
			for (int i = 0; i < headers.length; i++) {
				headerCell = headerRow.createCell(i);
				headerCell.setCellValue(headers[i]);
			}
			rowNum = 1;
		}

		// Create data rows
		for (Invoice invoice : invoices) {
			Patient patient = invoice.getTreatment().getAppointment().getPatient();
			String salutation = (patient.getSalutation() == Salutation.CUSTOM) ? patient.getCustomSalutation()
					: patient.getSalutation().name();

			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(invoice.getInvoiceID().toString());
			row.createCell(1).setCellValue(invoice.getInvoiceDate().toString());
			row.createCell(2).setCellValue(salutation + " " + patient.getPatientName());
			row.createCell(3).setCellValue(invoice.getTreatmentCost().toString());
			row.createCell(4).setCellValue(invoice.getPrescriptionCost().toString());
			row.createCell(5).setCellValue(invoice.getTotalAmount().toString());
			row.createCell(6).setCellValue(invoice.getInvoiceStatus().name());
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