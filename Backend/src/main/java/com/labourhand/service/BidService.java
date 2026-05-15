package com.labourhand.service;

import com.labourhand.dto.BidDto;
import com.labourhand.model.*;
import com.labourhand.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BidService {

    private final BidRepository bidRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final UserService userService;

    public List<BidDto.Response> getBidsForProject(Long projectId) {
        return bidRepository.findByProjectId(projectId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** Worker's own bids OR Owner's received bids */
    public List<BidDto.Response> getMyBids() {
        User user = userService.getCurrentUser();
        if (user.getRole() == User.Role.OWNER) {
            List<Project> projects = projectRepository.findByOwnerId(user.getId());
            return projects.stream()
                           .flatMap(p -> bidRepository.findByProjectId(p.getId()).stream())
                           .map(this::toResponse)
                           .collect(Collectors.toList());
        } else {
            return bidRepository.findByWorkerId(user.getId())
                    .stream().map(this::toResponse).collect(Collectors.toList());
        }
    }

    public BidDto.Response getById(Long id) {
        Bid b = bidRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bid not found: " + id));
        return toResponse(b);
    }

    /** Any WORKER (independent or team) can place a bid */
    @Transactional
    public BidDto.Response placeBid(BidDto.Request req) {
        User worker = userService.getCurrentUser();
        if (worker.getRole() != User.Role.WORKER) {
            throw new IllegalStateException("Only workers can place bids");
        }
        // Prevent duplicate bids on same project
        bidRepository.findByProjectIdAndWorkerId(req.getProjectId(), worker.getId())
                .ifPresent(b -> {
                    throw new IllegalStateException("You already bid on this project");
                });

        Project project = projectRepository.findById(req.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (project.getStatus() != Project.Status.OPEN_FOR_BIDS) {
            throw new IllegalStateException("Project is not open for bids");
        }

        Bid bid = Bid.builder()
                .projectId(req.getProjectId())
                .workerId(worker.getId())
                .amount(req.getAmount())
                .estimatedDays(req.getEstimatedDays())
                .message(req.getMessage())
                .status(Bid.Status.PENDING)
                .teamWorkerIds(req.getTeamWorkerIds() != null ? req.getTeamWorkerIds() : new java.util.ArrayList<>())
                .build();
        return toResponse(bidRepository.save(bid));
    }

    @Transactional
    public BidDto.Response updateBid(Long bidId, BidDto.Request req) {
        User worker = userService.getCurrentUser();
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new RuntimeException("Bid not found"));
        if (!bid.getWorkerId().equals(worker.getId())) {
            throw new IllegalStateException("Not your bid");
        }
        if (bid.getStatus() != Bid.Status.PENDING) {
            throw new IllegalStateException("Can only edit pending bids");
        }
        bid.setAmount(req.getAmount());
        bid.setEstimatedDays(req.getEstimatedDays());
        bid.setMessage(req.getMessage());
        return toResponse(bidRepository.save(bid));
    }

    @Transactional
    public void withdrawBid(Long bidId) {
        User worker = userService.getCurrentUser();
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new RuntimeException("Bid not found"));
        if (!bid.getWorkerId().equals(worker.getId())) {
            throw new IllegalStateException("Not your bid");
        }
        bidRepository.deleteById(bidId);
    }

    @Transactional
    public BidDto.Response acceptBid(Long bidId) {
        User owner = userService.getCurrentUser();
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new RuntimeException("Bid not found"));
        Project project = projectRepository.findById(bid.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (!project.getOwnerId().equals(owner.getId())) {
            throw new IllegalStateException("Not your project");
        }
        // Accept this bid
        bid.setStatus(Bid.Status.ACCEPTED);
        bidRepository.save(bid);
        // Reject all other bids
        bidRepository.findByProjectId(project.getId()).stream()
                .filter(b -> !b.getId().equals(bidId))
                .forEach(b -> {
                    b.setStatus(Bid.Status.REJECTED);
                    bidRepository.save(b);
                });
        // Update project status
        project.setStatus(Project.Status.IN_PROGRESS);
        project.setAcceptedBidId(bidId);
        projectRepository.save(project);
        // Increment worker's completed jobs counter (on project completion, not here;
        // just track accept)
        return toResponse(bid);
    }

    @Transactional
    public BidDto.Response rejectBid(Long bidId) {
        User owner = userService.getCurrentUser();
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new RuntimeException("Bid not found"));
        Project project = projectRepository.findById(bid.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (!project.getOwnerId().equals(owner.getId())) {
            throw new IllegalStateException("Not your project");
        }
        bid.setStatus(Bid.Status.REJECTED);
        return toResponse(bidRepository.save(bid));
    }

    // ── DTO Mapper ─────────────────────────────────────────────────────────
    BidDto.Response toResponse(Bid bid) {
        BidDto.Response r = new BidDto.Response();
        r.setId(bid.getId());
        r.setProjectId(bid.getProjectId());
        r.setWorkerId(bid.getWorkerId());
        r.setAmount(bid.getAmount());
        r.setEstimatedDays(bid.getEstimatedDays());
        r.setMessage(bid.getMessage());
        r.setTeamWorkerIds(bid.getTeamWorkerIds() != null ? new java.util.ArrayList<>(bid.getTeamWorkerIds()) : new java.util.ArrayList<>());
        r.setStatus(bid.getStatus().name());
        r.setAmountPaid(bid.getAmountPaid());
        r.setAmountRemaining(bid.getAmount() - bid.getAmountPaid());
        r.setSubmittedAt(bid.getSubmittedAt() != null
                ? bid.getSubmittedAt().toString()
                : "");

        projectRepository.findById(bid.getProjectId()).ifPresent(p -> {
            r.setProjectTitle(p.getTitle());
            r.setProjectLocation(p.getLocation());
            r.setProjectStatus(p.getStatus().name());
            r.setProgress(p.getProgress());
        });

        userRepository.findById(bid.getWorkerId()).ifPresent(u -> {
            r.setWorkerName(u.getName());
            r.setWorkerAvatar(u.getAvatar());
            workerProfileRepository.findById(u.getId()).ifPresent(wp -> {
                r.setWorkerRating(wp.getRating());
                r.setWorkerCompletedJobs(wp.getCompletedJobs());
                r.setWorkerVerified(wp.isSkillsIndiaVerified());
            });
        });
        return r;
    }
}
