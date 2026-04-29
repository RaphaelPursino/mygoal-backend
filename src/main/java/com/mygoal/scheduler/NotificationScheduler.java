package com.mygoal.scheduler;

import com.mygoal.entity.Goal;
import com.mygoal.entity.User;
import com.mygoal.repository.GoalRepository;
import com.mygoal.repository.UserRepository;
import com.mygoal.service.AIService;
import com.mygoal.service.GoalService;
import com.mygoal.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final AIService aiService;
    private final MailService mailService;
    private final GoalService goalService;

    // Envia e-mails motivacionais de hora em hora das 8h às 22h
    @Scheduled(cron = "0 0 8,13,20 * * *")
    public void sendHourlyMotivationalEmails() {
        log.info("Iniciando envio de e-mails motivacionais...");

        List<User> users = userRepository.findUsersWithActiveGoals();
        log.info("Usuários com metas ativas: {}", users.size());

        for (User user : users) {
            try {
                List<Goal> activeGoals = goalRepository.findActiveGoalsByUserId(user.getId());
                if (activeGoals.isEmpty()) continue;

                Goal goal = activeGoals.get(0);
                String phrase = aiService.generateMotivationalPhrase(goal);
                mailService.sendMotivationalEmail(user, goal, phrase);

            } catch (Exception e) {
                log.error("Erro ao processar notificação para {}: {}", user.getEmail(), e.getMessage());
            }
        }

        log.info("Envio de e-mails concluído.");
    }

    // Gera missões do dia para todas as metas ativas às 6h da manhã
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