package com.healthcaremngnt.service;

import com.healthcaremngnt.exceptions.EmailAlreadyExistsException;
import com.healthcaremngnt.exceptions.UsernameAlreadyExistsException;
import com.healthcaremngnt.model.User;
import com.healthcaremngnt.model.UserDetails;

public interface UserService {

	User register(User user) throws EmailAlreadyExistsException, UsernameAlreadyExistsException;

	User findByEmailID(String emailID);

	UserDetails findUserDetailsByID(Long userID);

	void updateUserDetails(UserDetails userDetails);

	UserDetails findUserDetailsByName(String userName);

	User findByUserName(String userName);

	User getUserByPatientID(Long patientID);

}