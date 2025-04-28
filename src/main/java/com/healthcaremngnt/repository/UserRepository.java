package com.healthcaremngnt.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.healthcaremngnt.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	User findByUserID(Long userID);
	// If findByUserID is just an alias for findById, remove it and use the
	// inherited findById
	// Optional<User> findByUserID(Long userID); <- Remove if redundant, use
	// findById

	Optional<User> findByUserName(String userName);

	Optional<User> findByEmailID(String emailID);

	// Custom query to search for users
	@Query("SELECT u FROM User u WHERE " + "(:roleID = 0 OR u.role.roleID = :roleID) AND "
			+ "(:userName IS NULL OR u.userName LIKE %:userName%) AND "
			+ "(:emailID IS NULL OR u.emailID LIKE %:emailID%)")
	List<User> searchUsers(@Param("roleID") int roleID, @Param("userName") String userName,
			@Param("emailID") String emailID);

	@Query("SELECT u FROM User u WHERE u.role.roleID = 1 AND " + "(:name IS NULL OR u.userName LIKE %:name%) "
			+ "AND (:emailID IS NULL OR u.emailID LIKE %:emailID%)")
	List<User> searchAdminUsers(@Param("name") String name, @Param("emailID") String emailID);

	@Query("SELECT u FROM User u WHERE u.userID = (SELECT p.user.userID FROM Patient p WHERE p.patientID = :patientID)")
	Optional<User> findUserByPatientID(@Param("patientID")Long patientID);

	// Example of a derived query (if needed):
	// Optional<User> findByUserNameIgnoreCase(String userName); // Case-insensitive search

	// Example using Pageable for pagination (if needed)
	/*
	 * @Query("SELECT u FROM User u WHERE ...") Page<User>
	 * searchUsersWithPagination(@Param("roleId") int roleId, ..., Pageable
	 * pageable);
	 */

}