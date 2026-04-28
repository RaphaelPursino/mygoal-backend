package com.mygoal.controller;

import com.mygoal.dto.goal.GoalRequest;
import com.mygoal.dto.goal.GoalResponse;
import com.mygoal.dto.goal.MissionResponse;
import com.mygoal.entity.User;
import com.mygoal.service.GoalService;
import com.mygoal.service.MissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
@Slf4j
public class GoalController {

    private final GoalService goalService;
    private final MissionService missionService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Authentication: {}", auth);
        log.debug("Principal: {}", auth != null ? auth.getPrincipal() : "null");
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Usuário não autenticado");
        }
        return (User) auth.getPrincipal();
    }

    @GetMapping
    public ResponseEntity<List<GoalResponse>> list() {
        return ResponseEntity.ok(goalService.listByUser(getCurrentUser()));
    }

    @PostMapping
    public ResponseEntity<GoalResponse> create(@Valid @RequestBody GoalRequest req) {
        log.debug("Criando meta: {}", req.getTitle());
        return ResponseEntity.ok(goalService.create(getCurrentUser(), req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(goalService.getById(getCurrentUser(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        goalService.delete(getCurrentUser(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/missions/{missionId}/complete")
    public ResponseEntity<MissionResponse> completeMission(@PathVariable UUID missionId) {
        return ResponseEntity.ok(missionService.completeMission(getCurrentUser(), missionId));
    }
}