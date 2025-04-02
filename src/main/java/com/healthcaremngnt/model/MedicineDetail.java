package com.healthcaremngnt.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicinedetails")
public class MedicineDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "medicine_id")
	private Long medicineID;

	@Column(name = "medicine_name", nullable = false, length = 255)
	private String medicineName;

	@Column(name = "medicine_type", nullable = false, length = 50)
	private String medicineType;

	@Column(name = "variation", nullable = false, length = 255)
	private String variation;

	@Column(name = "cost", precision = 10, scale = 2, nullable = false)
	private BigDecimal cost;

	/**
	 * @return the medicineID
	 */
	public Long getMedicineID() {
		return medicineID;
	}

	/**
	 * @param medicineID the medicineID to set
	 */
	public void setMedicineID(Long medicineID) {
		this.medicineID = medicineID;
	}

	/**
	 * @return the medicineName
	 */
	public String getMedicineName() {
		return medicineName;
	}

	/**
	 * @param medicineName the medicineName to set
	 */
	public void setMedicineName(String medicineName) {
		this.medicineName = medicineName;
	}

	/**
	 * @return the medicineType
	 */
	public String getMedicineType() {
		return medicineType;
	}

	/**
	 * @param medicineType the medicineType to set
	 */
	public void setMedicineType(String medicineType) {
		this.medicineType = medicineType;
	}

	/**
	 * @return the variation
	 */
	public String getVariation() {
		return variation;
	}

	/**
	 * @param variation the variation to set
	 */
	public void setVariation(String variation) {
		this.variation = variation;
	}

	/**
	 * @return the cost
	 */
	public BigDecimal getCost() {
		return cost;
	}

	/**
	 * @param cost the cost to set
	 */
	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}

	@Override
	public String toString() {
		return "MedicineDetail [medicineID=" + medicineID + ", medicineName=" + medicineName + ", medicineType="
				+ medicineType + ", variation=" + variation + ", cost=" + cost + "]";
	}

}