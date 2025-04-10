package com.healthcaremngnt.job.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.healthcaremngnt.model.Invoice;

@Component
public class BillingReportItemProcessor implements ItemProcessor<Invoice, Invoice> {

	private static final Logger logger = LogManager.getLogger(BillingReportItemProcessor.class);

	@Override
	public Invoice process(Invoice invoice) throws Exception {

		logger.info("BillingReportItemProcessor::: process()");
		// Check for conditions for report generation ie, based on billing date etc.

		return invoice;

	}

}