package com.healthcaremngnt.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "pwdresettokens")
public class PasswordResetToken {

	private static final int EXPIRATION = 10;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "token_id")
	private Long tokenID;

	@Column(name = "token", nullable = false, unique = true)
	private String token;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "email_id", nullable = false)
	private String emailID;

	@Column(name = "expiration_Time", nullable = false)
	private LocalDateTime expirationTime;

	public PasswordResetToken() {
		this.token = UUID.randomUUID().toString();
		this.expirationTime = LocalDateTime.now().plusMinutes(EXPIRATION);
	}

	public PasswordResetToken(User user, String emailID) {
		this.user = user;
		this.emailID = emailID;
		this.token = UUID.randomUUID().toString();
		this.expirationTime = LocalDateTime.now().plusMinutes(EXPIRATION);
	}

	public boolean isExpired() {
		return LocalDateTime.now().isAfter(this.expirationTime);
	}

	// Getters and Setters

	public Long getTokenID() {
		return tokenID;
	}

	public void setTokenID(Long tokenID) {
		this.tokenID = tokenID;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getEmailID() {
		return emailID;
	}

	public void setEmailID(String emailID) {
		this.emailID = emailID;
	}

	public LocalDateTime getExpirationTime() {
		return expirationTime;
	}

	public void setExpirationTime(LocalDateTime expirationTime) {
		this.expirationTime = expirationTime;
	}

	public static int getExpiration() {
		return EXPIRATION;
	}

	@Override
	public String toString() {
		return "PasswordResetToken [tokenID=" + tokenID + ", token=" + token + ", emailID=" + emailID
				+ ", expirationTime=" + expirationTime + "]";
	}
}
