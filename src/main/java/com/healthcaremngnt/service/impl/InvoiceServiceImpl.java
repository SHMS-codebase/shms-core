package com.healthcaremngnt.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcaremngnt.model.Invoice;
import com.healthcaremngnt.repository.InvoiceRepository;
import com.healthcaremngnt.service.InvoiceService;

@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

	private static final Logger logger = LogManager.getLogger(InvoiceServiceImpl.class);

	private final InvoiceRepository invoiceRepository;

	public InvoiceServiceImpl(InvoiceRepository invoiceRepository) {
		this.invoiceRepository = invoiceRepository;
	}

	@Override
	public Invoice generateInvoice(Invoice invoice) {
		logger.info("Generating invoice for ID: {}", invoice.getInvoiceID());

		validateInvoice(invoice);

		Invoice savedInvoice = invoiceRepository.save(invoice);
		logger.info("Invoice successfully generated with ID: {}", savedInvoice.getInvoiceID());

		return savedInvoice;
	}

	private void validateInvoice(Invoice invoice) {
		if (invoice == null) {
			throw new IllegalArgumentException("Invoice cannot be null.");
		}
	}

	@Override
	public Invoice getInvoiceDetails(Long invoiceID) {
		logger.info("Fetching invoice details for ID: {}", invoiceID);

		return invoiceRepository.findById(invoiceID)
				.orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceID));
	}

	@Override
	public Invoice updateInvoiceStatus(Invoice invoice) {
		logger.info("Updating status for Invoice ID: {}", invoice.getInvoiceID());

		validateInvoice(invoice);

		Invoice updatedInvoice = invoiceRepository.save(invoice);
		logger.info("Invoice status successfully updated for ID: {}", updatedInvoice.getInvoiceID());

		return updatedInvoice;
	}

}