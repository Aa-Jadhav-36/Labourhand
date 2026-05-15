package com.labourhand.service;

import com.labourhand.dto.ProjectDto;
import com.labourhand.model.*;
import com.labourhand.repository.*;
import com.labourhand.dto.ProgressUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final BidRepository bidRepository;
    private final UserService userService;
    private final OwnerProfileRepository ownerProfileRepository;
    private final ProgressUpdateRepository progressUpdateRepository;
    private final PaymentRepository paymentRepository;

    public List<ProjectDto.Response> getAllOpenProjects() {
        return projectRepository.findByStatus(Project.Status.OPEN_FOR_BIDS)
                .stream()
                .map(p -> toResponse(p, null))
                .collect(Collectors.toList());
    }

    public List<ProjectDto.Response> getNearbyProjects(double lat, double lng, double radius) {
        // Uses native Haversine query; rows are [project columns..., distance]
        List<Object[]> rows = projectRepository.findNearbyProjectsRaw(lat, lng, radius);
        return rows.stream().map(row -> {
            Long id = ((Number) row[0]).longValue();
            Project p = projectRepository.findById(id).orElse(null);
            if (p == null)
                return null;
            Double dist = ((Number) row[row.length - 1]).doubleValue();
            return toResponse(p, dist);
        }).filter(r -> r != null).collect(Collectors.toList());
    }

    public List<ProjectDto.Response> getProjectsByCategory(String category) {
        // Only show OPEN_FOR_BIDS jobs when browsing by category
        return projectRepository.findByCategoryAndStatus(category, Project.Status.OPEN_FOR_BIDS)
                .stream().map(p -> toResponse(p, null)).collect(Collectors.toList());
    }

    public List<ProjectDto.Response> getNearbyProjectsByCategory(double lat, double lng, double radius, String category) {
        List<Object[]> rows = projectRepository.findNearbyProjectsByCategoryRaw(lat, lng, radius, category);
        return rows.stream().map(row -> {
            Long id = ((Number) row[0]).longValue();
            Project p = projectRepository.findById(id).orElse(null);
            if (p == null) return null;
            Double dist = ((Number) row[row.length - 1]).doubleValue();
            return toResponse(p, dist);
        }).filter(r -> r != null).collect(Collectors.toList());
    }

    public List<ProjectDto.Response> getMyProjects() {
        User owner = userService.getCurrentUser();
        return projectRepository.findByOwnerId(owner.getId())
                .stream().map(p -> toResponse(p, null)).collect(Collectors.toList());
    }

    public ProjectDto.Response getById(Long id) {
        Project p = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found: " + id));
        return toResponse(p, null);
    }

    @Transactional
    public ProjectDto.Response createProject(ProjectDto.Request req) {
        User owner = userService.getCurrentUser();
        Project p = Project.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .category(req.getCategory())
                .budget(req.getBudget())
                .timelineDays(req.getTimelineDays())
                .location(req.getLocation())
                .lat(req.getLat())
                .lng(req.getLng())
                .deadline(req.getDeadline())
                .ownerId(owner.getId())
                .status(Project.Status.OPEN_FOR_BIDS)
                .build();
        p = projectRepository.save(p);
        // Increment projectsPosted counter
        ownerProfileRepository.findById(owner.getId()).ifPresent(op -> {
            op.setProjectsPosted(op.getProjectsPosted() + 1);
            ownerProfileRepository.save(op);
        });
        return toResponse(p, null);
    }

    @Transactional
    public ProjectDto.Response updateProject(Long id, ProjectDto.Request req) {
        Project p = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (req.getTitle() != null)
            p.setTitle(req.getTitle());
        if (req.getDescription() != null)
            p.setDescription(req.getDescription());
        if (req.getCategory() != null)
            p.setCategory(req.getCategory());
        if (req.getBudget() > 0)
            p.setBudget(req.getBudget());
        if (req.getTimelineDays() > 0)
            p.setTimelineDays(req.getTimelineDays());
        if (req.getLocation() != null)
            p.setLocation(req.getLocation());
        if (req.getLat() != null)
            p.setLat(req.getLat());
        if (req.getLng() != null)
            p.setLng(req.getLng());
        if (req.getDeadline() != null)
            p.setDeadline(req.getDeadline());
        return toResponse(projectRepository.save(p), null);
    }

    @Transactional
    public ProjectDto.Response updateProgress(Long id, ProjectDto.ProgressRequest req) {
        Project p = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        if (req.getStatus() == Project.Status.COMPLETED && p.getAcceptedBidId() != null) {
            Bid bid = bidRepository.findById(p.getAcceptedBidId()).orElseThrow(() -> new RuntimeException("Bid not found"));
            if (bid.getAmountPaid() < bid.getAmount()) {
                throw new RuntimeException("Cannot complete project. Unpaid balance remains.");
            }
        }

        p.setProgress(req.getProgress());
        if (req.getStatus() != null)
            p.setStatus(req.getStatus());
        return toResponse(projectRepository.save(p), null);
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    public List<ProgressUpdateDto.Response> getProjectUpdates(Long projectId) {
        return progressUpdateRepository.findByProjectIdOrderByCreatedAtAsc(projectId).stream()
                .map(this::toProgressUpdateDtoResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProgressUpdateDto.Response addProgressUpdate(Long projectId, ProgressUpdateDto.Request req) {
        User currentUser = userService.getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOwnerId().equals(currentUser.getId())) {
             // Must be the assigned worker if not the owner
             if (project.getAcceptedBidId() != null) {
                 Bid b = bidRepository.findById(project.getAcceptedBidId()).orElseThrow(() -> new RuntimeException("Bid not found"));
                 if (!b.getWorkerId().equals(currentUser.getId())) {
                     throw new IllegalStateException("Not authorized to post updates for this project");
                 }
                 
                 // Validate demand amount
                 if (Boolean.TRUE.equals(req.getPaymentDemanded()) && req.getDemandAmount() != null) {
                     long remaining = b.getAmount() - b.getAmountPaid();
                     if (req.getDemandAmount() > remaining) {
                         throw new RuntimeException("Demand amount (" + req.getDemandAmount() + ") exceeds remaining budget (" + remaining + ")");
                     }
                 }
             } else {
                 throw new IllegalStateException("Project not assigned yet");
             }
        }

        ProgressUpdate update = ProgressUpdate.builder()
                .projectId(projectId)
                .userId(currentUser.getId())
                .progressPercentage(req.getProgressPercentage())
                .comment(req.getComment())
                .paymentDemanded(req.getPaymentDemanded())
                .demandAmount(req.getDemandAmount())
                .build();
        
        update = progressUpdateRepository.save(update);

        // Sync project progress
        if (req.getProgressPercentage() != null) {
            project.setProgress(req.getProgressPercentage());
            if (req.getProgressPercentage() == 100 && project.getStatus() != Project.Status.COMPLETED) {
                if (project.getAcceptedBidId() != null) {
                    Bid bid = bidRepository.findById(project.getAcceptedBidId()).orElse(null);
                    if (bid != null && bid.getAmountPaid() >= bid.getAmount()) {
                        project.setStatus(Project.Status.COMPLETED);
                    }
                }
            }
            projectRepository.save(project);
        }

        ProgressUpdateDto.Response r = new ProgressUpdateDto.Response();
        r.setId(update.getId());
        r.setProjectId(update.getProjectId());
        r.setUserId(update.getUserId());
        r.setProgressPercentage(update.getProgressPercentage());
        r.setComment(update.getComment());
        r.setPaymentDemanded(update.getPaymentDemanded());
        r.setDemandAmount(update.getDemandAmount());
        r.setCreatedAt(update.getCreatedAt().toString());
        r.setUserName(currentUser.getName());
        r.setUserRole(currentUser.getRole().name());
        r.setUserAvatar(currentUser.getAvatar());
        return r;
    }

    private ProgressUpdateDto.Response toProgressUpdateDtoResponse(ProgressUpdate update) {
        ProgressUpdateDto.Response r = new ProgressUpdateDto.Response();
        r.setId(update.getId());
        r.setProjectId(update.getProjectId());
        r.setUserId(update.getUserId());
        r.setProgressPercentage(update.getProgressPercentage());
        r.setComment(update.getComment());
        r.setPaymentDemanded(update.getPaymentDemanded());
        r.setDemandAmount(update.getDemandAmount());
        r.setCreatedAt(update.getCreatedAt().toString());
        
        // Fetch payment status if demanded
        if (Boolean.TRUE.equals(update.getPaymentDemanded())) {
            List<Payment> payments = paymentRepository.findByProgressUpdateId(update.getId());
            if (!payments.isEmpty()) {
                r.setPaymentStatus(payments.get(0).getStatus().name());
            } else {
                r.setPaymentStatus("PENDING");
            }
        }

        userRepository.findById(update.getUserId()).ifPresent(u -> {
            r.setUserName(u.getName());
            r.setUserRole(u.getRole().name());
            r.setUserAvatar(u.getAvatar());
        });
        return r;
    }

    // ── DTO Mapper ─────────────────────────────────────────────────────────
    public ProjectDto.Response toResponse(Project p, Double distanceKm) {
        ProjectDto.Response r = new ProjectDto.Response();
        r.setId(p.getId());
        r.setTitle(p.getTitle());
        r.setDescription(p.getDescription());
        r.setCategory(p.getCategory());
        r.setBudget(p.getBudget());
        r.setTimelineDays(p.getTimelineDays());
        r.setLocation(p.getLocation());
        r.setLat(p.getLat());
        r.setLng(p.getLng());
        r.setStatus(p.getStatus().name());
        r.setOwnerId(p.getOwnerId());
        r.setProgress(p.getProgress());
        r.setDeadline(p.getDeadline());
        r.setDistanceKm(distanceKm);
        r.setBidCount((int) bidRepository.findByProjectId(p.getId()).size());
        r.setPostedAt(formatRelative(p.getCreatedAt()));
        userRepository.findById(p.getOwnerId()).ifPresent(u -> r.setOwnerName(u.getName()));
        
        if (p.getAcceptedBidId() != null) {
            bidRepository.findById(p.getAcceptedBidId()).ifPresent(bid -> {
                r.setWorkerId(bid.getWorkerId());
                r.setAcceptedBidAmount(bid.getAmount());
                r.setAcceptedBidPaid(bid.getAmountPaid());
                userRepository.findById(bid.getWorkerId()).ifPresent(u -> r.setAcceptedBidWorkerName(u.getName()));
            });
        }
        
        return r;
    }

    private String formatRelative(LocalDateTime dt) {
        if (dt == null)
            return "";
        long hours = java.time.Duration.between(dt, LocalDateTime.now()).toHours();
        if (hours < 24)
            return hours + " hours ago";
        long days = hours / 24;
        return days + " day" + (days > 1 ? "s" : "") + " ago";
    }
}
