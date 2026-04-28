package com.mygoal.service;

import com.mygoal.dto.goal.MissionResponse;
import com.mygoal.entity.Goal;
import com.mygoal.entity.Mission;
import com.mygoal.entity.User;
import com.mygoal.repository.GoalRepository;
import com.mygoal.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final GoalRepository goalRepository;

    @Transactional
    public MissionResponse completeMission(User user, UUID missionId) {
        Mission mission = missionRepository.findByIdAndGoalUserId(missionId, user.getId())
                .orElseThrow(() -> new RuntimeException("Missão não encontrada"));

        if (mission.getCompleted()) {
            throw new RuntimeException("Missão já concluída");
        }

        mission.setCompleted(true);
        mission.setCompletedAt(LocalDateTime.now());
        missionRepository.save(mission);

        // Atualiza progresso da meta
        Goal goal = mission.getGoal();
        goal.setCompletedMissions(goal.getCompletedMissions() + 1);
        goal.recalculateProgress();
        goalRepository.save(goal);

        return MissionResponse.from(mission);
    }
}