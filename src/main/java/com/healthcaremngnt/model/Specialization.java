package com.healthcaremngnt.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "specializations")
public class Specialization {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "specialization_id")
	private int specializationID;

    @Column(name = "specialization_name", nullable = false, unique = true, length = 100)
	private String specializationName;

	/**
	 * @return the specializationID
	 */
	public int getSpecializationID() {
		return specializationID;
	}

	/**
	 * @param specializationID the specializationID to set
	 */
	public void setSpecializationID(int specializationID) {
		this.specializationID = specializationID;
	}

	/**
	 * @return the specializationName
	 */
	public String getSpecializationName() {
		return specializationName;
	}

	/**
	 * @param specializationName the specializationName to set
	 */
	public void setSpecializationName(String specializationName) {
		this.specializationName = specializationName;
	}

	@Override
	public String toString() {
		return "Specialization [specializationID=" + specializationID + ", specializationName=" + specializationName
				+ "]";
	}

}