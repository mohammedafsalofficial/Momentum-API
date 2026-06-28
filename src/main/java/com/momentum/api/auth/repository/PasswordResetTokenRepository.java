package com.momentum.api.auth.repository;

import com.momentum.api.auth.model.PasswordResetToken;
import com.momentum.api.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    @Modifying
    @Query("DELETE FROM PasswordResetToken AS prt WHERE prt.user = :user")
    void deleteAllByUser(User user);

    Optional<PasswordResetToken> findByToken(String token);
}
