package com.mygoal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygoal.entity.Goal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    @Value("${app.groq.api-key}")
    private String apiKey;

    @Value("${app.groq.api-url}")
    private String apiUrl;

    @Value("${app.groq.model}")
    private String model;

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    public record MissionData(String title, String description) {}

    public List<MissionData> generateDailyMissions(Goal goal) {
        String prompt = buildMissionPrompt(goal);
        String response = callGroq(prompt);
        return parseMissions(response);
    }

    public String generateMotivationalPhrase(Goal goal) {
        String prompt = String.format("""
            Você é um coach pessoal motivador e empático.
            O usuário tem a seguinte meta: "%s"
            Prazo: %s
            Progresso atual: %d%%
            
            Crie UMA frase motivacional curta (máximo 2 frases), personalizada para essa meta,
            que inspire o usuário a continuar. Seja específico sobre a meta, não genérico.
            Responda apenas com a frase, sem aspas ou formatação adicional.
            """,
                goal.getTitle(),
                goal.getTargetDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                goal.getProgressPercentage()
        );
        return callGroq(prompt).trim();
    }

    private String buildMissionPrompt(Goal goal) {
        return String.format("""
            Você é um coach pessoal especialista em produtividade e formação de hábitos.
            
            O usuário quer alcançar a seguinte meta: "%s"
            Observações do usuário: "%s"
            Prazo para concluir: %s
            Data de hoje: %s
            
            Crie exatamente 3 missões diárias práticas, específicas e realizáveis em um dia
            que ajudem esse usuário a progredir em direção à sua meta.
            
            Responda APENAS com um JSON válido no seguinte formato, sem texto adicional, sem markdown:
            [
              {"title": "título curto", "description": "descrição detalhada do que fazer"},
              {"title": "título curto", "description": "descrição detalhada do que fazer"},
              {"title": "título curto", "description": "descrição detalhada do que fazer"}
            ]
            """,
                goal.getTitle(),
                goal.getNotes() != null ? goal.getNotes() : "Nenhuma observação",
                goal.getTargetDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );
    }

    private String callGroq(String prompt) {
        int maxRetries = 3;
        int delayMs = 2000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Map<String, Object> body = Map.of(
                        "model", model,
                        "messages", List.of(
                                Map.of("role", "user", "content", prompt)
                        ),
                        "temperature", 0.7,
                        "max_tokens", 1024
                );

                String response = webClientBuilder.build()
                        .post()
                        .uri(apiUrl)
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .bodyValue(body)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                JsonNode root = objectMapper.readTree(response);
                return root.path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText();

            } catch (Exception e) {
                log.warn("Tentativa {} falhou: {}", attempt, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(delayMs);
                        delayMs *= 2;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    log.error("Todas as tentativas falharam para Groq API");
                    throw new RuntimeException("Erro ao gerar conteúdo com IA", e);
                }
            }
        }
        throw new RuntimeException("Erro ao gerar conteúdo com IA");
    }

    private List<MissionData> parseMissions(String jsonText) {
        try {
            String cleaned = jsonText
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            // Extrai apenas o array JSON se houver texto antes/depois
            int start = cleaned.indexOf('[');
            int end = cleaned.lastIndexOf(']');
            if (start >= 0 && end > start) {
                cleaned = cleaned.substring(start, end + 1);
            }

            JsonNode arr = objectMapper.readTree(cleaned);
            List<MissionData> missions = new ArrayList<>();

            for (JsonNode node : arr) {
                missions.add(new MissionData(
                        node.path("title").asText(),
                        node.path("description").asText()
                ));
            }

            return missions;
        } catch (Exception e) {
            log.error("Erro ao parsear missões da IA: {}", e.getMessage());
            return List.of(
                    new MissionData("Planejar o dia", "Reserve 10 minutos para planejar suas atividades do dia relacionadas à sua meta."),
                    new MissionData("Executar uma ação concreta", "Dedique 30 minutos trabalhando diretamente na sua meta."),
                    new MissionData("Revisar e refletir", "Ao final do dia, revise o que fez e anote o progresso.")
            );
        }
    }
}