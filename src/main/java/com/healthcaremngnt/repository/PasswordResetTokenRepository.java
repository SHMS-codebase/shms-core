package com.healthcaremngnt.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.healthcaremngnt.model.PasswordResetToken;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

	Optional<PasswordResetToken> findByToken(String token);

	@Query("SELECT p FROM PasswordResetToken p WHERE p.emailID = :emailID ORDER BY p.tokenID DESC")
	Optional<PasswordResetToken> findFirstByEmailIDOrderByTokenIDDesc(@Param("emailID") String emailID);
	
	// Derived Query Example (if needed):
    // Optional<PasswordResetToken> findByEmailID(String emailID);
	
}