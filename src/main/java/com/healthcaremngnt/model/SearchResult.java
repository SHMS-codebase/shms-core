package com.healthcaremngnt.model;

public class SearchResult {
	
	private Long userID;
	private String roleName;
	private String name;
	private String emailID;
	private String contactNumber;
	private String specialization;

	public SearchResult(Long userID, String roleName, String name, String emailID, String contactNumber, String specialization) {
		this.userID = userID;
		this.roleName = roleName;
		this.name = name;
		this.emailID = emailID;
		this.contactNumber = contactNumber;
		this.specialization = specialization;
	}

	/**
	 * @return the userID
	 */
	public Long getUserID() {
		return userID;
	}

	/**
	 * @param userID the userID to set
	 */
	public void setUserID(Long userID) {
		this.userID = userID;
	}
	
	/**
	 * @return the roleName
	 */
	public String getRoleName() {
		return roleName;
	}

	/**
	 * @param roleName the roleName to set
	 */
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the emailID
	 */
	public String getEmailID() {
		return emailID;
	}

	/**
	 * @param emailID the emailID to set
	 */
	public void setEmailID(String emailID) {
		this.emailID = emailID;
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

	@Override
	public String toString() {
		return "SearchResult [userID=" + userID + ", roleName=" + roleName + ", name=" + name + ", emailID=" + emailID + ", contactNumber="
				+ contactNumber + ", specialization=" + specialization + "]";
	}

}