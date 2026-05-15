package com.labourhand.service;

import com.labourhand.dto.MiscDto;
import com.labourhand.dto.UserDto;
import com.labourhand.model.*;
import com.labourhand.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkerService {

    private final UserRepository userRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final OwnerProfileRepository ownerProfileRepository;
    private final ReviewRepository reviewRepository;
    private final CertificationRepository certificationRepository;
    private final SkillRepository skillRepository;
    private final UserService userService;

    public List<UserDto> getAllWorkers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.WORKER)
                .map(u -> UserService.toDto(u, workerProfileRepository, ownerProfileRepository))
                .collect(Collectors.toList());
    }

    public UserDto getWorkerById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Worker not found: " + id));
        return UserService.toDto(user, workerProfileRepository, ownerProfileRepository);
    }

    public List<MiscDto.ReviewResponse> getWorkerReviews(Long workerId) {
        return reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(workerId)
                .stream().map(this::toReviewResponse).collect(Collectors.toList());
    }

    public List<MiscDto.CertificationResponse> getWorkerCertifications(Long workerId) {
        return certificationRepository.findByWorkerProfileId(workerId)
                .stream().map(c -> {
                    MiscDto.CertificationResponse r = new MiscDto.CertificationResponse();
                    r.setId(c.getId());
                    r.setName(c.getName());
                    r.setIssuer(c.getIssuer());
                    r.setYear(c.getYear());
                    return r;
                }).collect(Collectors.toList());
    }

    @Transactional
    public void updatePaymentDetails(com.labourhand.dto.WorkerPaymentDetailsDto req) {
        User worker = userService.getCurrentUser();
        WorkerProfile wp = workerProfileRepository.findById(worker.getId())
                .orElseThrow(() -> new RuntimeException("Worker profile not found"));
        
        if (req.getPaymentMethod() != null) wp.setPaymentMethod(req.getPaymentMethod());
        if (req.getBankAccountNo() != null && !req.getBankAccountNo().isEmpty()) wp.setBankAccountNo(req.getBankAccountNo());
        if (req.getBankName() != null && !req.getBankName().isEmpty()) wp.setBankName(req.getBankName());
        if (req.getIfscCode() != null && !req.getIfscCode().isEmpty()) wp.setIfscCode(req.getIfscCode());
        if (req.getHolderName() != null && !req.getHolderName().isEmpty()) wp.setHolderName(req.getHolderName());
        if (req.getUpiId() != null && !req.getUpiId().isEmpty()) wp.setUpiId(req.getUpiId());
        
        workerProfileRepository.save(wp);
    }

    @Transactional
    public MiscDto.CertificationResponse addCertification(MiscDto.CertificationRequest req) {
        User worker = userService.getCurrentUser();
        WorkerProfile wp = workerProfileRepository.findById(worker.getId())
                .orElseThrow(() -> new RuntimeException("Worker profile not found"));
        Certification cert = Certification.builder()
                .workerProfile(wp).name(req.getName())
                .issuer(req.getIssuer()).year(req.getYear())
                .build();
        cert = certificationRepository.save(cert);
        MiscDto.CertificationResponse r = new MiscDto.CertificationResponse();
        r.setId(cert.getId());
        r.setName(cert.getName());
        r.setIssuer(cert.getIssuer());
        r.setYear(cert.getYear());
        return r;
    }

    @Transactional
    public void deleteCertification(Long certId) {
        User worker = userService.getCurrentUser();
        certificationRepository.deleteByWorkerProfileIdAndId(worker.getId(), certId);
    }

    // ── Reviews ────────────────────────────────────────────────────────────
    @Transactional
    public MiscDto.ReviewResponse createReview(MiscDto.ReviewRequest req) {
        User reviewer = userService.getCurrentUser();
        Review review = Review.builder()
                .projectId(req.getProjectId())
                .reviewerId(reviewer.getId())
                .revieweeId(req.getRevieweeId())
                .rating(req.getRating())
                .comment(req.getComment())
                .duration(req.getDuration())
                .clientName(reviewer.getName())
                .build();
        review = reviewRepository.save(review);
        // Recompute worker average rating
        recalculateWorkerRating(req.getRevieweeId());
        return toReviewResponse(review);
    }

    public MiscDto.ReviewResponse getReviewById(Long id) {
        return toReviewResponse(reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found")));
    }

    public List<MiscDto.ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::toReviewResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteReview(Long id) {
        Review r = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        reviewRepository.deleteById(id);
        recalculateWorkerRating(r.getRevieweeId());
    }

    private void recalculateWorkerRating(Long workerId) {
        List<Review> reviews = reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(workerId);
        if (reviews.isEmpty())
            return;
        double avg = reviews.stream().mapToDouble(Review::getRating).average().orElse(0);
        workerProfileRepository.findById(workerId).ifPresent(wp -> {
            wp.setRating(Math.round(avg * 10.0) / 10.0);
            wp.setCompletedJobs(reviews.size());
            workerProfileRepository.save(wp);
        });
    }

    // ── Skills ─────────────────────────────────────────────────────────────
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    @Transactional
    public Skill createSkill(MiscDto.SkillRequest req) {
        return skillRepository.findByName(req.getName())
                .orElseGet(() -> skillRepository.save(Skill.builder().name(req.getName()).build()));
    }

    @Transactional
    public void deleteSkill(Long id) {
        skillRepository.deleteById(id);
    }

    // ── Mapper ─────────────────────────────────────────────────────────────
    private MiscDto.ReviewResponse toReviewResponse(Review r) {
        MiscDto.ReviewResponse dto = new MiscDto.ReviewResponse();
        dto.setId(r.getId());
        dto.setProjectId(r.getProjectId());
        dto.setProjectName(r.getProjectName());
        dto.setClientName(r.getClientName());
        dto.setRating(r.getRating());
        dto.setComment(r.getComment());
        dto.setDuration(r.getDuration());
        dto.setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt().toString() : "");
        return dto;
    }
}
