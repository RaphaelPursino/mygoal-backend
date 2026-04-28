// ========== UserRepository.java ==========
package com.mygoal.repository;

import com.mygoal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    boolean existsByEmail(String email);

    @Query("""
        SELECT DISTINCT u FROM User u
        JOIN u.goals g
        WHERE g.status = 'ACTIVE'
        AND u.notificationsEnabled = true
    """)
    List<User> findUsersWithActiveGoals();
}