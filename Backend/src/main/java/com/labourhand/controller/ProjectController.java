package com.labourhand.controller;

import com.labourhand.dto.ProjectDto;
import com.labourhand.dto.ProgressUpdateDto;
import com.labourhand.service.PaymentService;
import com.labourhand.service.ProjectService;
import com.labourhand.service.UserService;
import com.labourhand.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final PaymentService paymentService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<ProjectDto.Response>> getAll() {
        return ResponseEntity.ok(projectService.getAllOpenProjects());
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<ProjectDto.Response>> getNearby(
            @RequestParam(defaultValue = "12.9716") double lat,
            @RequestParam(defaultValue = "77.5946") double lng,
            @RequestParam(defaultValue = "10.0") double radius) {
        return ResponseEntity.ok(projectService.getNearbyProjects(lat, lng, radius));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ProjectDto.Response>> getMyProjects() {
        return ResponseEntity.ok(projectService.getMyProjects());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProjectDto.Response>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(projectService.getProjectsByCategory(category));
    }

    @GetMapping("/nearby/category/{category}")
    public ResponseEntity<List<ProjectDto.Response>> getNearbyByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "12.9716") double lat,
            @RequestParam(defaultValue = "77.5946") double lng,
            @RequestParam(defaultValue = "10.0") double radius) {
        return ResponseEntity.ok(projectService.getNearbyProjectsByCategory(lat, lng, radius, category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ProjectDto.Response> create(@Valid @RequestBody ProjectDto.Request req) {
        return ResponseEntity.ok(projectService.createProject(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto.Response> update(
            @PathVariable Long id, @RequestBody ProjectDto.Request req) {
        return ResponseEntity.ok(projectService.updateProject(id, req));
    }

    @PutMapping("/{id}/progress")
    public ResponseEntity<ProjectDto.Response> updateProgress(
            @PathVariable Long id, @RequestBody ProjectDto.ProgressRequest req) {
        return ResponseEntity.ok(projectService.updateProgress(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/updates")
    public ResponseEntity<List<ProgressUpdateDto.Response>> getUpdates(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectUpdates(id));
    }

    @PostMapping("/{id}/updates")
    public ResponseEntity<ProgressUpdateDto.Response> addUpdate(
            @PathVariable Long id, @Valid @RequestBody ProgressUpdateDto.Request req) {
        return ResponseEntity.ok(projectService.addProgressUpdate(id, req));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<com.labourhand.dto.PaymentDto.Response> payDemand(
            @PathVariable Long id, @Valid @RequestBody com.labourhand.dto.PaymentDto.Request req) {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(paymentService.processPayment(id, req, currentUser.getId()));
    }

    @GetMapping("/{id}/payments")
    public ResponseEntity<List<com.labourhand.dto.PaymentDto.Response>> getPayments(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentsByProjectId(id));
    }
}
