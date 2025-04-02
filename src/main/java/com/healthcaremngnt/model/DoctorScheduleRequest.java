package com.healthcaremngnt.model;

import com.healthcaremngnt.enums.ScheduleStatus;

public class DoctorScheduleRequest {

	private Long doctorID;
	private String availableDate;
	private String startTime;
	private String endTime;
	private Long availableCount;
	private ScheduleStatus scheduleStatus;

	public DoctorScheduleRequest(Long doctorID, String availableDate, String startTime, String endTime,
			Long availableCount, ScheduleStatus scheduleStatus) {
		this.doctorID = doctorID;
		this.availableDate = availableDate;
		this.startTime = startTime;
		this.endTime = endTime;
		this.availableCount = availableCount;
		this.scheduleStatus = scheduleStatus;
	}

	/**
	 * @return the doctorID
	 */
	public Long getDoctorID() {
		return doctorID;
	}

	/**
	 * @param doctorID the doctorID to set
	 */
	public void setDoctorID(Long doctorID) {
		this.doctorID = doctorID;
	}

	/**
	 * @return the availableDate
	 */
	public String getAvailableDate() {
		return availableDate;
	}

	/**
	 * @param availableDate the availableDate to set
	 */
	public void setAvailableDate(String availableDate) {
		this.availableDate = availableDate;
	}

	/**
	 * @return the startTime
	 */
	public String getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the endTime
	 */
	public String getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	/**
	 * @return the availableCount
	 */
	public Long getAvailableCount() {
		return availableCount;
	}

	/**
	 * @param availableCount the availableCount to set
	 */
	public void setAvailableCount(Long availableCount) {
		this.availableCount = availableCount;
	}

	public ScheduleStatus getScheduleStatus() {
		return scheduleStatus;
	}

	public void setScheduleStatus(ScheduleStatus scheduleStatus) {
		this.scheduleStatus = scheduleStatus;
	}

	@Override
	public String toString() {
		return "DoctorScheduleRequest [doctorID=" + doctorID + ", availableDate=" + availableDate + ", startTime="
				+ startTime + ", endTime=" + endTime + ", availableCount=" + availableCount + ", scheduleStatus="
				+ scheduleStatus + "]";
	}

}