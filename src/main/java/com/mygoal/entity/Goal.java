package com.mygoal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "goals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GoalStatus status = GoalStatus.ACTIVE;

    @Column(name = "progress_percentage", nullable = false)
    @Builder.Default
    private Integer progressPercentage = 0;

    @Column(name = "total_missions", nullable = false)
    @Builder.Default
    private Integer totalMissions = 0;

    @Column(name = "completed_missions", nullable = false)
    @Builder.Default
    private Integer completedMissions = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Mission> missions;

    public void recalculateProgress() {
        if (this.totalMissions == 0) {
            this.progressPercentage = 0;
            return;
        }
        this.progressPercentage = (int) Math.round(
                (double) this.completedMissions / this.totalMissions * 100
        );
        if (this.progressPercentage >= 100) {
            this.status = GoalStatus.COMPLETED;
        }
    }

    public enum GoalStatus {
        ACTIVE, COMPLETED, ABANDONED
    }
}