package com.momentum.api.auth.repository;

import com.momentum.api.auth.model.RefreshToken;
import com.momentum.api.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("UPDATE RefreshToken AS rt SET rt.revoked = true WHERE rt.id = :id")
    void revokeRefreshTokenById(long id);

    @Modifying
    @Query("UPDATE RefreshToken AS rt SET rt.revoked = true WHERE rt.familyId = :familyId")
    void revokeAllByFamilyId(String familyId);

    @Modifying
    @Query("DELETE FROM RefreshToken AS rt WHERE rt.user = :user")
    void deleteAllByUser(User user);
}
