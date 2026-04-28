package com.mygoal.dto.goal;

import com.mygoal.entity.Mission;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class MissionResponse {
    private UUID id;
    private String title;
    private String description;
    private LocalDate missionDate;
    private Boolean completed;
    private LocalDateTime completedAt;

    public static MissionResponse from(Mission mission) {
        return MissionResponse.builder()
                .id(mission.getId())
                .title(mission.getTitle())
                .description(mission.getDescription())
                .missionDate(mission.getMissionDate())
                .completed(mission.getCompleted())
                .completedAt(mission.getCompletedAt())
                .build();
    }
}