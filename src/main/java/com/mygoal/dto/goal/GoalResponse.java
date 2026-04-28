package com.mygoal.dto.goal;

import com.mygoal.entity.Goal;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class GoalResponse {
    private UUID id;
    private String title;
    private String notes;
    private LocalDate targetDate;
    private String status;
    private Integer progressPercentage;
    private Integer totalMissions;
    private Integer completedMissions;
    private LocalDateTime createdAt;
    private List<MissionResponse> todayMissions;

    public static GoalResponse from(Goal goal) {
        return GoalResponse.builder()
                .id(goal.getId())
                .title(goal.getTitle())
                .notes(goal.getNotes())
                .targetDate(goal.getTargetDate())
                .status(goal.getStatus().name())
                .progressPercentage(goal.getProgressPercentage())
                .totalMissions(goal.getTotalMissions())
                .completedMissions(goal.getCompletedMissions())
                .createdAt(goal.getCreatedAt())
                .build();
    }
}