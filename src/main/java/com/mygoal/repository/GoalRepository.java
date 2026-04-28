// ========== GoalRepository.java ==========
package com.mygoal.repository;

import com.mygoal.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID> {

    List<Goal> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Goal> findByUserIdAndStatus(UUID userId, Goal.GoalStatus status);

    Optional<Goal> findByIdAndUserId(UUID id, UUID userId);

    @Query("""
        SELECT g FROM Goal g
        WHERE g.user.id = :userId
        AND g.status = 'ACTIVE'
        ORDER BY g.targetDate ASC
    """)
    List<Goal> findActiveGoalsByUserId(UUID userId);
}