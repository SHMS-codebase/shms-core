package com.healthcaremngnt.service;

import java.util.Optional;

import com.healthcaremngnt.exceptions.EmailAlreadyExistsException;
import com.healthcaremngnt.exceptions.UsernameAlreadyExistsException;
import com.healthcaremngnt.model.User;
import com.healthcaremngnt.model.UserDetails;

public interface UserService {

	User register(User user) throws EmailAlreadyExistsException, UsernameAlreadyExistsException;

	Optional<User> findByEmailID(String emailID);

	Optional<UserDetails> findUserDetailsByID(Long userID);

	void updateUserDetails(UserDetails userDetails);

	Optional<UserDetails> findUserDetailsByName(String userName);

	Optional<User> findByUserName(String userName);

}