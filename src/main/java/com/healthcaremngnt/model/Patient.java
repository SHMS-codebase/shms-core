package com.healthcaremngnt.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.healthcaremngnt.enums.Gender;
import com.healthcaremngnt.enums.Salutation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "patients")
public class Patient {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "patient_id")
	private Long patientID;

	@Enumerated(EnumType.STRING)
	@Column(name = "salutation")
	private Salutation salutation; // Predefined values

	@Column(name = "custom_salutation", length = 50)
	private String customSalutation; // For custom entries

	@Column(name = "patient_name", nullable = false, length = 100)
	private String patientName;

	@Column(name = "dob", nullable = false)
	private LocalDate dob;

	@Enumerated(EnumType.STRING)
	@Column(name = "gender", nullable = false, length = 20)
	private Gender gender;

	@Column(name = "contact_number", length = 15)
	private String contactNumber;

	@Column(name = "address", length = 255)
	private String address;

	@Column(name = "created_date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
	private LocalDateTime createdDate;

	@Column(name = "updated_date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private LocalDateTime updatedDate;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", unique = true)
	private User user;

	/**
	 * @return the patientID
	 */
	public Long getPatientID() {
		return patientID;
	}

	/**
	 * @param patientID the patientID to set
	 */
	public void setPatientID(Long patientID) {
		this.patientID = patientID;
	}

	/**
	 * @return the salutation
	 */
	public Salutation getSalutation() {
		return salutation;
	}

	/**
	 * @param salutation the salutation to set
	 */
	public void setSalutation(Salutation salutation) {
		if (salutation == Salutation.CUSTOM) {
			this.salutation = null; // Avoid ENUM conflict
		} else {
			this.salutation = salutation;
			this.customSalutation = null; // Clear custom if predefined is chosen
		}
	}

	/**
	 * @return the customSalutation
	 */
	public String getCustomSalutation() {
		return customSalutation;
	}

	/**
	 * @param customSalutation the customSalutation to set
	 */

	public void setCustomSalutation(String customSalutation) {
		if (customSalutation != null && !customSalutation.isEmpty()) {
			this.salutation = Salutation.CUSTOM; // Flag ENUM as "CUSTOM"
		}
		this.customSalutation = customSalutation;
	}

	/**
	 * @return the patientName
	 */
	public String getPatientName() {
		return patientName;
	}

	/**
	 * @param patientName the patientName to set
	 */
	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	/**
	 * @return the dob
	 */
	public LocalDate getDob() {
		return dob;
	}

	/**
	 * @param dob the dob to set
	 */
	public void setDob(LocalDate dob) {
		this.dob = dob;
	}

	/**
	 * @return the gender
	 */
	public Gender getGender() {
		return gender;
	}

	/**
	 * @param gender the gender to set
	 */
	public void setGender(Gender gender) {
		this.gender = gender;
	}

	/**
	 * @return the contactNumber
	 */
	public String getContactNumber() {
		return contactNumber;
	}

	/**
	 * @param contactNumber the contactNumber to set
	 */
	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the createdDate
	 */
	public LocalDateTime getCreatedDate() {
		return createdDate;
	}

	/**
	 * @param createdDate the createdDate to set
	 */
	public void setCreatedDate(LocalDateTime createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * @return the updatedDate
	 */
	public LocalDateTime getUpdatedDate() {
		return updatedDate;
	}

	/**
	 * @param updatedDate the updatedDate to set
	 */
	public void setUpdatedDate(LocalDateTime updatedDate) {
		this.updatedDate = updatedDate;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	@PrePersist
	protected void onCreate() {
		createdDate = LocalDateTime.now();
		updatedDate = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedDate = LocalDateTime.now();
	}

	@Override
	public String toString() {
		return "Patient [patientID=" + patientID + ", salutation=" + salutation + ", customSalutation="
				+ customSalutation + ", patientName=" + patientName + ", dob=" + dob + ", gender=" + gender
				+ ", contactNumber=" + contactNumber + ", address=" + address + ", createdDate=" + createdDate
				+ ", updatedDate=" + updatedDate + ", user=" + user + "]";
	}

}