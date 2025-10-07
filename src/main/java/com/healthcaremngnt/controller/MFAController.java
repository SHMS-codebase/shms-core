package com.healthcaremngnt.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/mfa")
public class MFAController {

	private static final Logger logger = LogManager.getLogger(MFAController.class);

	public MFAController() {
		logger.debug("MFAController Constructor");
	}

}