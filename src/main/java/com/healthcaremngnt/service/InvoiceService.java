package com.healthcaremngnt.service;

import com.healthcaremngnt.model.Invoice;

public interface InvoiceService {

	Invoice generateInvoice(Invoice invoice);

	Invoice getInvoiceDetails(Long invoiceID);

	Invoice updateInvoiceStatus(Invoice invoice);

}
