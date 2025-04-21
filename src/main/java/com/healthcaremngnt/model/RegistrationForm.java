package com.healthcaremngnt.model;

import java.time.LocalDate;

import com.healthcaremngnt.enums.Gender;
import com.healthcaremngnt.enums.Salutation;

public class RegistrationForm {

	private String role;

	// Admin Data
	private String adminName;
	private String adminEmailID;

	// Doctor Data
	private String doctorName;
	private String doctorEmailID;
	private String doctorContactNumber;
	private String doctorAddress;
	private String qualification;
	private String specialization;
	private String experience;
	private String licenseNumber;

	// Patient Data
	private Salutation salutation;
	private String customSalutation;
	private String patientName;
	private String patientEmailID;
	private String patientContactNumber;
	private String patientAddress;
	private LocalDate dob;
	private Gender gender;

	/**
	 * @return the role
	 */
	public String getRole() {
		return role;
	}

	/**
	 * @param role the role to set
	 */
	public void setRole(String role) {
		this.role = role;
	}

	/**
	 * @return the adminName
	 */
	public String getAdminName() {
		return adminName;
	}

	/**
	 * @param adminName the adminName to set
	 */
	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}

	/**
	 * @return the adminEmailID
	 */
	public String getAdminEmailID() {
		return adminEmailID;
	}

	/**
	 * @param adminEmailID the adminEmailID to set
	 */
	public void setAdminEmailID(String adminEmailID) {
		this.adminEmailID = adminEmailID;
	}

	/**
	 * @return the doctorName
	 */
	public String getDoctorName() {
		return doctorName;
	}

	/**
	 * @param doctorName the doctorName to set
	 */
	public void setDoctorName(String doctorName) {
		this.doctorName = doctorName;
	}

	/**
	 * @return the doctorEmailID
	 */
	public String getDoctorEmailID() {
		return doctorEmailID;
	}

	/**
	 * @param doctorEmailID the doctorEmailID to set
	 */
	public void setDoctorEmailID(String doctorEmailID) {
		this.doctorEmailID = doctorEmailID;
	}

	/**
	 * @return the doctorContactNumber
	 */
	public String getDoctorContactNumber() {
		return doctorContactNumber;
	}

	/**
	 * @param doctorContactNumber the doctorContactNumber to set
	 */
	public void setDoctorContactNumber(String doctorContactNumber) {
		this.doctorContactNumber = doctorContactNumber;
	}

	/**
	 * @return the doctorAddress
	 */
	public String getDoctorAddress() {
		return doctorAddress;
	}

	/**
	 * @param doctoraddress the doctorAddress to set
	 */
	public void setDoctorAddress(String doctorAddress) {
		this.doctorAddress = doctorAddress;
	}

	/**
	 * @return the qualification
	 */
	public String getQualification() {
		return qualification;
	}

	/**
	 * @param qualification the qualification to set
	 */
	public void setQualification(String qualification) {
		this.qualification = qualification;
	}

	/**
	 * @return the specialization
	 */
	public String getSpecialization() {
		return specialization;
	}

	/**
	 * @param specialization the specialization to set
	 */
	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}

	/**
	 * @return the experience
	 */
	public String getExperience() {
		return experience;
	}

	/**
	 * @param experience the experience to set
	 */
	public void setExperience(String experience) {
		this.experience = experience;
	}

	/**
	 * @return the licenseNumber
	 */
	public String getLicenseNumber() {
		return licenseNumber;
	}

	/**
	 * @param licenseNumber the licenseNumber to set
	 */
	public void setLicenseNumber(String licenseNumber) {
		this.licenseNumber = licenseNumber;
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
		this.salutation = salutation;
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
	 * @return the patientEmailID
	 */
	public String getPatientEmailID() {
		return patientEmailID;
	}

	/**
	 * @param patientEmailID the patientEmailID to set
	 */
	public void setPatientEmailID(String patientEmailID) {
		this.patientEmailID = patientEmailID;
	}

	/**
	 * @return the patientContactNumber
	 */
	public String getPatientContactNumber() {
		return patientContactNumber;
	}

	/**
	 * @param patientContactNumber the patientContactNumber to set
	 */
	public void setPatientContactNumber(String patientContactNumber) {
		this.patientContactNumber = patientContactNumber;
	}

	/**
	 * @return the patientAddress
	 */
	public String getPatientAddress() {
		return patientAddress;
	}

	/**
	 * @param patientAddress the patientAddress to set
	 */
	public void setPatientAddress(String patientAddress) {
		this.patientAddress = patientAddress;
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

	@Override
	public String toString() {
		return "RegistrationForm [role=" + role + ", adminName=" + adminName + ", adminEmailID=" + adminEmailID
				+ ", doctorName=" + doctorName + ", doctorEmailID=" + doctorEmailID + ", doctorContactNumber="
				+ doctorContactNumber + ", doctorAddress=" + doctorAddress + ", qualification=" + qualification
				+ ", specialization=" + specialization + ", experience=" + experience + ", licenseNumber="
				+ licenseNumber + ", salutation=" + salutation + ", customSalutation=" + customSalutation
				+ ", patientName=" + patientName + ", patientEmailID=" + patientEmailID + ", patientContactNumber="
				+ patientContactNumber + ", patientAddress=" + patientAddress + ", dob=" + dob + ", gender=" + gender
				+ "]";
	}

}