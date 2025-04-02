package com.healthcaremngnt.job.reader;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.WeekFields;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.healthcaremngnt.model.Invoice;
import com.healthcaremngnt.repository.InvoiceRepository;

@Component
@StepScope
public class FinanceReportItemReader implements ItemReader<Invoice> {

	private static final Logger logger = LogManager.getLogger(FinanceReportItemReader.class);

	@Autowired
	private InvoiceRepository invoiceRepository;

	private Iterator<Invoice> invoiceIterator;

	@Value("#{jobParameters['datePicker'] ?: '2025-01-01'}")
	private String dateString;

	@Value("#{jobParameters['reportType'] ?: 'daily'}")
	private String reportType;

	private boolean noInvoicesAvailable = false;

	@Override
	public Invoice read()
			throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		logger.info("FinanceReportItemReader::: read()");
		if (invoiceIterator == null) {

			try {
				LocalDate queryDate = parseDate(dateString, reportType);
				if (queryDate == null) {
					noInvoicesAvailable = true; // Set the flag
					logger.error("Invalid date format: {}", dateString);
					return null; // Return null to indicate no invoices available
				}
				logger.debug("queryDate: {}", queryDate);

				List<Invoice> invoices = fetchInvoices(queryDate, reportType);
				logger.debug("invoices: {}", invoices);
				logger.debug("Read {} invoices total", invoices.size());

				if (invoices != null && !invoices.isEmpty()) {
					invoiceIterator = invoices.iterator();
				} else {
					noInvoicesAvailable = true; // Set the flag
					invoiceIterator = List.<Invoice>of().iterator();
					logger.warn("No invoices found for the given date and report type.");
				}
			} catch (Exception e) {
				logger.error("Error in invoiceItemReader: {}", e.getMessage());
				noInvoicesAvailable = true; // Set the flag
				invoiceIterator = List.<Invoice>of().iterator();
			}

		}

		if (invoiceIterator.hasNext()) {
			return invoiceIterator.next();
		} else {
			return null; // No more invoices
		}
	}

	private LocalDate parseDate(String dateString, String reportType) {
		try {
			logger.info("Inside parseDate(): {}", dateString);
			if ("yearly".equalsIgnoreCase(reportType)) {
				int year = Integer.parseInt(dateString);
				return LocalDate.of(year, 1, 1); // Start of the year
			} else if ("monthly".equalsIgnoreCase(reportType) && dateString.matches("\\d{4}-\\d{2}")) {
				YearMonth yearMonth = YearMonth.parse(dateString);
				return yearMonth.atDay(1); // Start of the month
			} else if ("weekly".equalsIgnoreCase(reportType) && dateString.matches("\\d{4}-W\\d{2}")) {
				WeekFields weekFields = WeekFields.of(Locale.getDefault());
				int year = Integer.parseInt(dateString.substring(0, 4));
				int week = Integer.parseInt(dateString.substring(6));
				return LocalDate.ofYearDay(year, 1).with(weekFields.weekOfYear(), week);
			} else {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				return LocalDate.parse(dateString, formatter);
			}
		} catch (DateTimeParseException | NumberFormatException e) {
			logger.error("Error parsing date: {}", e.getMessage());
			return null;
		}
	}

	private List<Invoice> fetchInvoices(LocalDate queryDate, String reportType) {
		switch (reportType.toLowerCase()) {
		case "daily":
			return invoiceRepository.findByInvoiceDate(queryDate);
		case "weekly":
			LocalDate startOfWeek = queryDate.minusDays(queryDate.getDayOfWeek().getValue() - 1);
			LocalDate endOfWeek = startOfWeek.plusDays(6);
			return invoiceRepository.findByInvoiceDateBetween(startOfWeek, endOfWeek);
		case "monthly":
			LocalDate startOfMonth = queryDate.withDayOfMonth(1);
			LocalDate endOfMonth = queryDate.withDayOfMonth(queryDate.lengthOfMonth());
			return invoiceRepository.findByInvoiceDateBetween(startOfMonth, endOfMonth);
		case "yearly":
			LocalDate startOfYear = queryDate.withDayOfYear(1);
			LocalDate endOfYear = queryDate.withDayOfYear(queryDate.lengthOfYear());
			return invoiceRepository.findByInvoiceDateBetween(startOfYear, endOfYear);
		default:
			throw new IllegalArgumentException("Invalid report type: " + reportType);
		}
	}

	public boolean isNoinvoicesAvailable() {
		return noInvoicesAvailable;
	}

}