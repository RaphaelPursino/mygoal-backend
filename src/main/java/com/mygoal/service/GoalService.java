package com.mygoal.service;

import com.mygoal.dto.goal.GoalRequest;
import com.mygoal.dto.goal.GoalResponse;
import com.mygoal.dto.goal.MissionResponse;
import com.mygoal.entity.Goal;
import com.mygoal.entity.Mission;
import com.mygoal.entity.User;
import com.mygoal.repository.GoalRepository;
import com.mygoal.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoalService {

    private final GoalRepository goalRepository;
    private final MissionRepository missionRepository;
    private final AIService aiService;

    @Transactional
    public GoalResponse create(User user, GoalRequest request) {
        Goal goal = Goal.builder()
                .user(user)
                .title(request.getTitle())
                .notes(request.getNotes())
                .targetDate(request.getTargetDate())
                .build();

        goal = goalRepository.save(goal);

        // Tenta gerar missões, mas não falha se der erro na IA
        try {
            List<Mission> missions = generateMissionsForToday(goal);
            goal.setTotalMissions(missions.size());
            goalRepository.save(goal);

            GoalResponse response = GoalResponse.from(goal);
            response.setTodayMissions(missions.stream().map(MissionResponse::from).toList());
            return response;
        } catch (Exception e) {
            log.error("Erro ao gerar missões via IA: {}", e.getMessage());
            // Retorna a meta mesmo sem missões
            return GoalResponse.from(goal);
        }
    }

    public List<GoalResponse> listByUser(User user) {
        return goalRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(goal -> {
                    GoalResponse r = GoalResponse.from(goal);
                    List<MissionResponse> today = missionRepository
                            .findByGoalIdAndMissionDateOrderByCreatedAtAsc(goal.getId(), LocalDate.now())
                            .stream().map(MissionResponse::from).toList();
                    r.setTodayMissions(today);
                    return r;
                })
                .toList();
    }

    public GoalResponse getById(User user, UUID goalId) {
        Goal goal = goalRepository.findByIdAndUserId(goalId, user.getId())
                .orElseThrow(() -> new RuntimeException("Meta não encontrada"));

        GoalResponse response = GoalResponse.from(goal);
        List<MissionResponse> today = missionRepository
                .findByGoalIdAndMissionDateOrderByCreatedAtAsc(goal.getId(), LocalDate.now())
                .stream().map(MissionResponse::from).toList();
        response.setTodayMissions(today);
        return response;
    }

    @Transactional
    public void delete(User user, UUID goalId) {
        Goal goal = goalRepository.findByIdAndUserId(goalId, user.getId())
                .orElseThrow(() -> new RuntimeException("Meta não encontrada"));
        goalRepository.delete(goal);
    }

    @Transactional
    public List<Mission> generateMissionsForToday(Goal goal) {
        // Evita duplicar missões do mesmo dia
        if (missionRepository.existsByGoalIdAndMissionDate(goal.getId(), LocalDate.now())) {
            return missionRepository.findByGoalIdAndMissionDateOrderByCreatedAtAsc(
                    goal.getId(), LocalDate.now()
            );
        }

        List<AIService.MissionData> missionData = aiService.generateDailyMissions(goal);

        List<Mission> missions = missionData.stream()
                .map(data -> Mission.builder()
                        .goal(goal)
                        .title(data.title())
                        .description(data.description())
                        .missionDate(LocalDate.now())
                        .build())
                .toList();

        return missionRepository.saveAll(missions);
    }
}