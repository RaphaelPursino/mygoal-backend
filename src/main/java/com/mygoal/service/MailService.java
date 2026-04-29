// ========== MailService.java ==========
package com.mygoal.service;

import com.mygoal.entity.Goal;
import com.mygoal.entity.NotificationLog;
import com.mygoal.entity.User;
import com.mygoal.repository.NotificationLogRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;
    private final NotificationLogRepository notificationLogRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendMotivationalEmail(User user, Goal goal, String phrase) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "MyGoal");
            helper.setTo(user.getEmail());
            helper.setSubject("💪 Sua meta te espera: " + goal.getTitle());
            helper.setText(buildEmailHtml(user.getName(), goal, phrase), true);

            mailSender.send(message);

            // Salva log
            notificationLogRepository.save(NotificationLog.builder()
                    .user(user)
                    .goal(goal)
                    .message(phrase)
                    .status("SENT")
                    .build());

            log.info("E-mail motivacional enviado para: {}", user.getEmail());
            log.info("Tentando enviar e-mail para: {} sobre meta: {}", user.getEmail(), goal.getTitle());

        } catch (Exception e) {
            log.error("Erro ao enviar e-mail para {}: {}", user.getEmail(), e.getMessage());

            notificationLogRepository.save(NotificationLog.builder()
                    .user(user)
                    .goal(goal)
                    .message(phrase)
                    .status("FAILED")
                    .build());
        }
    }

    private String buildEmailHtml(String name, Goal goal, String phrase) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <style>
                body { font-family: Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 0; }
                .container { max-width: 600px; margin: 40px auto; background: white;
                             border-radius: 12px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
                .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                          padding: 40px 30px; text-align: center; }
                .header h1 { color: white; margin: 0; font-size: 28px; }
                .header p { color: rgba(255,255,255,0.85); margin: 8px 0 0; font-size: 14px; }
                .body { padding: 30px; }
                .greeting { font-size: 18px; color: #333; margin-bottom: 20px; }
                .phrase-box { background: #f0f4ff; border-left: 4px solid #667eea;
                              padding: 20px; border-radius: 8px; margin: 20px 0; }
                .phrase-box p { margin: 0; font-size: 16px; color: #444; line-height: 1.6; font-style: italic; }
                .goal-box { background: #fff8e1; border-radius: 8px; padding: 16px; margin: 20px 0; }
                .goal-box .label { font-size: 12px; color: #888; text-transform: uppercase; margin-bottom: 4px; }
                .goal-box .title { font-size: 18px; color: #333; font-weight: bold; }
                .progress-bar { background: #e0e0e0; border-radius: 20px; height: 12px; margin: 8px 0; }
                .progress-fill { background: linear-gradient(90deg, #667eea, #764ba2);
                                 height: 12px; border-radius: 20px; }
                .btn { display: inline-block; background: #667eea; color: white; padding: 14px 32px;
                       border-radius: 8px; text-decoration: none; font-size: 16px; font-weight: bold;
                       margin: 20px 0; }
                .footer { background: #f8f8f8; padding: 20px; text-align: center;
                          font-size: 12px; color: #999; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1>MyGoal</h1>
                  <p>Sua jornada rumo ao sucesso</p>
                </div>
                <div class="body">
                  <p class="greeting">Olá, <strong>%s</strong>! 👋</p>
                  <div class="phrase-box">
                    <p>%s</p>
                  </div>
                  <div class="goal-box">
                    <div class="label">Sua meta ativa</div>
                    <div class="title">%s</div>
                    <div class="progress-bar">
                      <div class="progress-fill" style="width: %d%%"></div>
                    </div>
                    <p style="margin: 4px 0; font-size: 13px; color: #666;">Progresso: %d%%</p>
                  </div>
                  <p>Não deixe o dia passar sem completar suas missões de hoje! Cada pequena ação te aproxima do seu objetivo.</p>
                  <center>
                    <a href="${app.frontend-url}/goals/%s" class="btn">Ver minhas missões de hoje</a>
                  </center>
                </div>
                <div class="footer">
                  <p>Você está recebendo este e-mail porque tem metas ativas no MyGoal.</p>
                  <p>© 2025 MyGoal. Todos os direitos reservados.</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                name, phrase, goal.getTitle(),
                goal.getProgressPercentage(), goal.getProgressPercentage(),
                goal.getId()
        );
    }
}