package com.labourhand.controller;

import com.labourhand.dto.MiscDto;
import com.labourhand.dto.UserDto;
import com.labourhand.model.Skill;
import com.labourhand.service.WorkerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;
    private final com.labourhand.service.RatingService ratingService;
    private final com.labourhand.service.UserService userService;

    // ── Workers ────────────────────────────────────────────────────────────
    @GetMapping("/api/workers")
    public ResponseEntity<List<UserDto>> getAllWorkers() {
        return ResponseEntity.ok(workerService.getAllWorkers());
    }

    @GetMapping("/api/workers/{id}")
    public ResponseEntity<UserDto> getWorker(@PathVariable Long id) {
        return ResponseEntity.ok(workerService.getWorkerById(id));
    }

    @GetMapping("/api/workers/{id}/reviews")
    public ResponseEntity<List<MiscDto.ReviewResponse>> getReviews(@PathVariable Long id) {
        return ResponseEntity.ok(workerService.getWorkerReviews(id));
    }

    @GetMapping("/api/workers/{id}/certifications")
    public ResponseEntity<List<MiscDto.CertificationResponse>> getCertifications(@PathVariable Long id) {
        return ResponseEntity.ok(workerService.getWorkerCertifications(id));
    }

    @PostMapping("/api/workers/certifications")
    public ResponseEntity<MiscDto.CertificationResponse> addCertification(
            @Valid @RequestBody MiscDto.CertificationRequest req) {
        return ResponseEntity.ok(workerService.addCertification(req));
    }

    @DeleteMapping("/api/workers/certifications/{id}")
    public ResponseEntity<Void> deleteCertification(@PathVariable Long id) {
        workerService.deleteCertification(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/workers/payment-details")
    public ResponseEntity<Void> updatePaymentDetails(
            @Valid @RequestBody com.labourhand.dto.WorkerPaymentDetailsDto req) {
        workerService.updatePaymentDetails(req);
        return ResponseEntity.noContent().build();
    }

    // ── Reviews ────────────────────────────────────────────────────────────
    @GetMapping("/api/reviews")
    public ResponseEntity<List<MiscDto.ReviewResponse>> getAllReviews() {
        return ResponseEntity.ok(workerService.getAllReviews());
    }

    @GetMapping("/api/reviews/{id}")
    public ResponseEntity<MiscDto.ReviewResponse> getReview(@PathVariable Long id) {
        return ResponseEntity.ok(workerService.getReviewById(id));
    }

    @PostMapping("/api/reviews")
    public ResponseEntity<com.labourhand.dto.RatingDto.Response> createRating(
            @Valid @RequestBody com.labourhand.dto.RatingDto.Request req) {
        return ResponseEntity.ok(ratingService.addRating(req, userService.getCurrentUser().getId()));
    }

    @GetMapping("/api/workers/{id}/ratings")
    public ResponseEntity<List<com.labourhand.dto.RatingDto.Response>> getRatings(@PathVariable Long id) {
        return ResponseEntity.ok(ratingService.getRatingsForWorker(id));
    }

    @DeleteMapping("/api/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        workerService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    // ── Skills ─────────────────────────────────────────────────────────────
    @GetMapping("/api/skills")
    public ResponseEntity<List<Skill>> getAllSkills() {
        return ResponseEntity.ok(workerService.getAllSkills());
    }

    @PostMapping("/api/skills")
    public ResponseEntity<Skill> createSkill(@Valid @RequestBody MiscDto.SkillRequest req) {
        return ResponseEntity.ok(workerService.createSkill(req));
    }

    @DeleteMapping("/api/skills/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        workerService.deleteSkill(id);
        return ResponseEntity.noContent().build();
    }
}
