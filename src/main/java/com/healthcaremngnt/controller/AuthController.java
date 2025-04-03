package com.healthcaremngnt.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.healthcaremngnt.constants.MessageConstants;
import com.healthcaremngnt.constants.RequestParamConstants;
import com.healthcaremngnt.exceptions.InvalidTokenException;
import com.healthcaremngnt.exceptions.TokenExpiredException;
import com.healthcaremngnt.model.ForgotPasswordEmailForm;
import com.healthcaremngnt.model.ForgotPasswordUsernameForm;
import com.healthcaremngnt.model.PasswordResetToken;
import com.healthcaremngnt.model.User;
import com.healthcaremngnt.service.EmailService;
import com.healthcaremngnt.service.PasswordResetService;
import com.healthcaremngnt.service.PasswordResetTokenService;
import com.healthcaremngnt.service.UserService;
import com.healthcaremngnt.util.TokenValidator;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/auth")
public class AuthController {

	private static final Logger logger = LogManager.getLogger(AuthController.class);

	private final EmailService emailService;
	private final UserService userService;
	private final PasswordResetTokenService tokenService;
	private final PasswordResetService passwordService;

	public AuthController(EmailService emailService, UserService userService, PasswordResetTokenService tokenService,
			PasswordResetService passwordService) {
		this.emailService = emailService;
		this.userService = userService;
		this.tokenService = tokenService;
		this.passwordService = passwordService;
	}

	@GetMapping("/forgot-password")
	public String showForgotPasswordForm(Model model) {
		logger.info("Forgot Password Form will be loaded");

		if (!model.containsAttribute("forgotPasswordEmailForm")) {
			model.addAttribute("forgotPasswordEmailForm", new ForgotPasswordEmailForm());
		}

		if (!model.containsAttribute("forgotPasswordUsernameForm")) {
			model.addAttribute("forgotPasswordUsernameForm", new ForgotPasswordUsernameForm());
		}

		return "forgotpassword";
	}

	@PostMapping("/forgot-password/email")
	public String processForgotPasswordByEmail(
			@Valid @ModelAttribute("forgotPasswordEmailForm") ForgotPasswordEmailForm form, BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {

		logger.info("Processing forgot password request for email: {}", form.getEmail());

		if (bindingResult.hasErrors()) {
			return handleValidationErrors("forgotPasswordEmailForm", form, bindingResult, redirectAttributes);
		}

		return processPasswordResetByEmail(form.getEmail(), redirectAttributes);
	}

	private String processPasswordResetByEmail(String email, RedirectAttributes redirectAttributes) {
		try {
			User user = userService.findByEmailID(email);

			if (user != null) {
				logger.debug("User found with email: {}", email);
				emailService.sendPasswordResetEmail(user.getEmailID(), user);
				redirectAttributes.addFlashAttribute("message", MessageConstants.PWD_LINK_MSG);
			} else {
				logger.error("{}: {}", MessageConstants.USER_NOT_FOUND, email);
				redirectAttributes.addFlashAttribute("message", MessageConstants.PWD_LINK_GENERIC_MSG);
			}
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.PWD_LINK_ERROR, e);
			redirectAttributes.addFlashAttribute("errorMessage", MessageConstants.PWD_LINK_ERROR);
		}

		return "redirect:/auth/forgot-password";
	}

	@PostMapping("/forgot-password/username")
	public String processForgotPasswordByUsername(
			@Valid @ModelAttribute("forgotPasswordUsernameForm") ForgotPasswordUsernameForm form,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) {

		logger.info("Processing forgot password request for username: {}", form.getUsername());

		if (bindingResult.hasErrors()) {
			return handleValidationErrors("forgotPasswordUsernameForm", form, bindingResult, redirectAttributes);
		}

		return processPasswordReset(form.getUsername(), redirectAttributes);
	}

	// Helper method to handle validation errors
	private String handleValidationErrors(String formName, Object form, BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult." + formName, bindingResult);
		redirectAttributes.addFlashAttribute(formName, form);
		return "redirect:/auth/forgot-password";
	}

	private String processPasswordReset(String username, RedirectAttributes redirectAttributes) {
		try {
			User user = userService.findByUserName(username);

			if (user != null) {
				logger.debug("User found in system with username: {}", username);
				emailService.sendPasswordResetEmail(user.getEmailID(), user);
				redirectAttributes.addFlashAttribute("message", MessageConstants.PWD_LINK_MSG);
			} else {
				logger.error("{}: {}", MessageConstants.USER_NOT_FOUND, username);
				redirectAttributes.addFlashAttribute("message", MessageConstants.PWD_LINK_GENERIC_MSG);
			}
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.PWD_LINK_ERROR, e);
			redirectAttributes.addFlashAttribute("errorMessage", MessageConstants.PWD_LINK_ERROR);
		}

		return "redirect:/auth/forgot-password";
	}

	@GetMapping("/reset-password")
	public String showResetPasswordForm(@RequestParam(RequestParamConstants.TOKEN) String token, Model model) {
		logger.info("Reset Password Form will be loaded");
		PasswordResetToken resetToken = tokenService.findByToken(token);

		if (resetToken != null) {
			String validationError = TokenValidator.validateResetToken(resetToken, model);
			if (validationError != null) {
				logger.error("{}: {}", MessageConstants.TOKEN_INVALID_OR_EXPIRED, token);
				model.addAttribute("tokenError", MessageConstants.TOKEN_INVALID_OR_EXPIRED);
				return "resetpassword";
			}
			logger.debug("The Token is valid");
			model.addAttribute("token", token);
		} else {
			logger.error("{}: {}", MessageConstants.TOKEN_NOT_FOUND, token);
			model.addAttribute("tokenError", MessageConstants.TOKEN_INVALID_OR_EXPIRED);
			return "resetpassword";
		}

		return "resetpassword";
	}

	@PostMapping("/reset-password")
	public String processResetPassword(@RequestParam(RequestParamConstants.TOKEN) String token,
			@RequestParam(RequestParamConstants.PASSWORD) String password, Model model)
			throws InvalidTokenException, TokenExpiredException {
		logger.info("Reset Password form is being processed");
		PasswordResetToken resetToken = tokenService.findByToken(token);

		if (resetToken != null) {
			String validationError = TokenValidator.validateResetToken(resetToken, model);
			if (validationError != null) {
				logger.error("{}: {}", MessageConstants.TOKEN_INVALID_OR_EXPIRED, token);
				model.addAttribute("tokenError", MessageConstants.TOKEN_INVALID_OR_EXPIRED);
				return "resetpassword";
			}

			logger.debug("Reset the User's Password in the Users table");

			try {
				passwordService.resetPassword(resetToken.getToken(), password);
				logger.debug("{}", MessageConstants.PASSWORD_RESET_SUCCESS);
				model.addAttribute("message", MessageConstants.PASSWORD_RESET_SUCCESS);
				return "resetpasswordsuccess";
			} catch (Exception e) {
				logger.error("{}: {}", MessageConstants.PASSWORD_RESET_FAILURE, e);
				model.addAttribute("errorMessage", MessageConstants.PASSWORD_RESET_FAILURE);
				return "resetpassword";
			}
		} else {
			logger.error("{}: {}", MessageConstants.TOKEN_NOT_FOUND, token);
			model.addAttribute("errorMessage", MessageConstants.TOKEN_NOT_FOUND);
			return "resetpassword";
		}
	}

	@GetMapping("/reset-password-success")
	public String showResetPasswordSuccessPage() {
		logger.info("Reset Password Success Page is being loaded");
		return "resetpasswordsuccess";
	}

	@GetMapping("/change-password")
	public String showChangePasswordForm(@RequestParam(RequestParamConstants.USER_ID) Long userID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Change Password Form will be loaded");
		logger.debug("userID: {}", userID);
		logger.debug("source: {}", source);

		model.addAttribute("userID", userID);
		model.addAttribute("source", source);

		return "changepassword";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam(RequestParamConstants.USER_ID) Long userID,
			@RequestParam(RequestParamConstants.SOURCE) String source,
			@RequestParam(RequestParamConstants.PASSWORD) String password,
			@RequestParam(RequestParamConstants.NEWPASSWORD) String newpassword, Model model) {

		logger.info("Change Password form is being processed");

		logger.debug("userID: {}", userID);
		logger.debug("source: {}", source);

		logger.debug("Reset the User's Password in the Users table");
		try {
			passwordService.resetPassword(userID, password, newpassword);
			logger.debug("{}", MessageConstants.PASSWORD_RESET_SUCCESS);
			model.addAttribute("message", MessageConstants.PASSWORD_RESET_SUCCESS);
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.PASSWORD_RESET_FAILURE, e);
			model.addAttribute("errorMessage", MessageConstants.PASSWORD_RESET_FAILURE);
		}

		model.addAttribute("userID", userID);
		model.addAttribute("source", source);

		return "changepassword";
	}

}