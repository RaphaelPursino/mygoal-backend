package com.mygoal.repository;

import com.mygoal.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MissionRepository extends JpaRepository<Mission, UUID> {

    List<Mission> findByGoalIdAndMissionDateOrderByCreatedAtAsc(UUID goalId, LocalDate date);

    List<Mission> findByGoalIdOrderByMissionDateDescCreatedAtAsc(UUID goalId);

    Optional<Mission> findByIdAndGoalUserId(UUID missionId, UUID userId);

    boolean existsByGoalIdAndMissionDate(UUID goalId, LocalDate date);

    @Query("""
    SELECT m FROM Mission m
    WHERE m.goal.id = :goalId
    AND m.missionDate < :date
    AND m.completed = false
    ORDER BY m.missionDate ASC, m.createdAt ASC
""")
    List<Mission> findPendingMissionsBeforeDate(UUID goalId, LocalDate date);
}