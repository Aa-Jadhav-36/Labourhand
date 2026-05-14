package com.labourhand.controller;

import com.labourhand.dto.MiscDto;
import com.labourhand.dto.UserDto;
import com.labourhand.service.ContractorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contractor")
@RequiredArgsConstructor
public class ContractorController {

    private final ContractorService contractorService;

    @GetMapping("/team")
    public ResponseEntity<List<UserDto>> getTeam() {
        return ResponseEntity.ok(contractorService.getTeam());
    }

    @GetMapping("/projects")
    public ResponseEntity<List<MiscDto.ContractorProjectResponse>> getActiveProjects() {
        return ResponseEntity.ok(contractorService.getActiveProjects());
    }

    @GetMapping("/earnings")
    public ResponseEntity<MiscDto.EarningsResponse> getEarnings(
            @RequestParam(defaultValue = "week") String period) {
        return ResponseEntity.ok(contractorService.getEarnings(period));
    }

    // ── Schedule ───────────────────────────────────────────────────────────
    @GetMapping("/schedule")
    public ResponseEntity<List<MiscDto.ScheduleEventResponse>> getSchedule() {
        return ResponseEntity.ok(contractorService.getSchedule());
    }

    @PostMapping("/schedule")
    public ResponseEntity<MiscDto.ScheduleEventResponse> createScheduleEvent(
            @Valid @RequestBody MiscDto.ScheduleEventRequest req) {
        return ResponseEntity.ok(contractorService.createScheduleEvent(req));
    }

    @PutMapping("/schedule/{id}")
    public ResponseEntity<MiscDto.ScheduleEventResponse> updateScheduleEvent(
            @PathVariable Long id, @RequestBody MiscDto.ScheduleEventRequest req) {
        return ResponseEntity.ok(contractorService.updateScheduleEvent(id, req));
    }

    @DeleteMapping("/schedule/{id}")
    public ResponseEntity<Void> deleteScheduleEvent(@PathVariable Long id) {
        contractorService.deleteScheduleEvent(id);
        return ResponseEntity.noContent().build();
    }
}
