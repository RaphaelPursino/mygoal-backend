package com.mygoal.dto.goal;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GoalRequest {
    @NotBlank(message = "Título da meta é obrigatório")
    private String title;

    private String notes;

    @NotNull(message = "Data limite é obrigatória")
    @Future(message = "A data deve ser no futuro")
    private LocalDate targetDate;
}