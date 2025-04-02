package com.healthcaremngnt.model;

import java.util.List;

public class DoctorScheduleWrapper {

	private List<DoctorSchedule> doctorScheduleList;

	/**
	 * @return the doctorScheduleList
	 */
	public List<DoctorSchedule> getDoctorScheduleList() {
		return doctorScheduleList;
	}

	/**
	 * @param doctorScheduleList the doctorScheduleList to set
	 */
	public void setDoctorScheduleList(List<DoctorSchedule> doctorScheduleList) {
		this.doctorScheduleList = doctorScheduleList;
	}

	@Override
	public String toString() {
		return "DoctorScheduleWrapper [doctorScheduleList=" + doctorScheduleList + "]";
	}

}