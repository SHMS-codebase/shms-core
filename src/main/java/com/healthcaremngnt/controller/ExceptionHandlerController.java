package com.healthcaremngnt.controller;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class ExceptionHandlerController {

	@ExceptionHandler(NoResourceFoundException.class)
	public String handleNotFoundError(Model model) {
		model.addAttribute("error", "404 - Page Not Found");
		model.addAttribute("message", "The page you are looking for does not exist.");
		return "error";
	}

	@ExceptionHandler(Exception.class)
	public String handleInternalServerError(Model model) {
		model.addAttribute("error", "500 - Internal Server Error");
		model.addAttribute("message", "Something went wrong. Please try again later.");
		return "error";
	}

	@ExceptionHandler(BindException.class)
	public String handleBadRequestError(Model model) {
		model.addAttribute("error", "400 - Bad Request");
		model.addAttribute("message", "Invalid request data. Please check your inputs.");
		return "error";
	}

	@ExceptionHandler(AuthenticationException.class)
	public String handleUnauthorizedError(Model model) {
		model.addAttribute("error", "401 - Unauthorized");
		model.addAttribute("message", "Authentication failed. Please log in.");
		return "error";
	}

	@ExceptionHandler(AccessDeniedException.class)
	public String handleForbiddenError(Model model) {
		model.addAttribute("error", "403 - Forbidden");
		model.addAttribute("message", "You don't have permission to access this resource.");
		return "error";
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public String handleMethodNotAllowedError(Model model) {
		model.addAttribute("error", "405 - Method Not Allowed");
		model.addAttribute("message", "The requested method is not supported.");
		return "error";
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public String handleUnsupportedMediaTypeError(Model model) {
		model.addAttribute("error", "415 - Unsupported Media Type");
		model.addAttribute("message", "The media type of the request is not supported.");
		return "error";
	}
}