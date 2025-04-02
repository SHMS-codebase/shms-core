package com.healthcaremngnt.service;

import java.util.Optional;

import com.healthcaremngnt.model.Invoice;

public interface InvoiceService {

	Invoice generateInvoice(Invoice invoice);

	Optional<Invoice> getInvoiceDetails(Long invoiceID);

	Invoice updateInvoiceStatus(Invoice invoice);

}
