package com.mygoal.scheduler;

import com.mygoal.entity.Goal;
import com.mygoal.repository.GoalRepository;
import com.mygoal.service.GoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final GoalRepository goalRepository;
    private final GoalService goalService;

    @Scheduled(cron = "0 0 6 * * *")
    public void generateDailyMissions() {
        log.info("Gerando missões diárias...");

        List<Goal> activeGoals = goalRepository.findAll()
                .stream()
                .filter(g -> g.getStatus() == Goal.GoalStatus.ACTIVE)
                .toList();

        for (Goal goal : activeGoals) {
            try {
                goalService.generateMissionsForToday(goal);
                goal.setTotalMissions(goal.getTotalMissions() + 3);
                goalRepository.save(goal);
            } catch (Exception e) {
                log.error("Erro ao gerar missões para meta {}: {}", goal.getId(), e.getMessage());
            }
        }

        log.info("Missões diárias geradas para {} metas.", activeGoals.size());
    }
}