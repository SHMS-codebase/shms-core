package com.healthcaremngnt.service.impl;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.healthcaremngnt.model.Invoice;
import com.healthcaremngnt.repository.InvoiceRepository;
import com.healthcaremngnt.service.InvoiceService;

@Service
public class InvoiceServiceImpl implements InvoiceService {

	private static final Logger logger = LogManager.getLogger(InvoiceServiceImpl.class);

	private final InvoiceRepository invoiceRepository;

	public InvoiceServiceImpl(InvoiceRepository invoiceRepository) {
		this.invoiceRepository = invoiceRepository;
	}

	@Override
	public Invoice generateInvoice(Invoice invoice) {

		logger.info("InvoiceServiceImpl::: generateInvoice()");
		
		return invoiceRepository.save(invoice);
	}

	@Override
	public Optional<Invoice> getInvoiceDetails(Long invoiceID) {
		
		logger.info("InvoiceServiceImpl::: getInvoiceDetails()");
		
		return invoiceRepository.findById(invoiceID);

	}

	@Override
	public Invoice updateInvoiceStatus(Invoice invoice) {

		logger.info("InvoiceServiceImpl::: updateInvoiceStatus()");
		
		return invoiceRepository.save(invoice);
	}

}
