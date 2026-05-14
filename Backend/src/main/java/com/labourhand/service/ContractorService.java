package com.labourhand.service;

import com.labourhand.dto.MiscDto;
import com.labourhand.dto.UserDto;
import com.labourhand.model.*;
import com.labourhand.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractorService {

    private final BidRepository bidRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final OwnerProfileRepository ownerProfileRepository;
    private final EarningRepository earningRepository;
    private final ScheduleEventRepository scheduleEventRepository;
    private final UserService userService;

    /**
     * "My Team" = distinct workers who had an accepted bid on a project owned by
     * me.
     * Any independent worker who won a bid is part of the team.
     */
    public List<UserDto> getTeam() {
        User owner = userService.getCurrentUser();
        List<Long> workerIds = bidRepository.findAcceptedWorkerIdsByOwnerId(owner.getId());
        return workerIds.stream()
                .map(id -> userRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .map(u -> UserService.toDto(u, workerProfileRepository, ownerProfileRepository))
                .collect(Collectors.toList());
    }

    /**
     * Owner's IN_PROGRESS and OPEN_FOR_BIDS projects formatted for contractor
     * dashboard
     */
    public List<MiscDto.ContractorProjectResponse> getActiveProjects() {
        User owner = userService.getCurrentUser();
        return projectRepository.findByOwnerId(owner.getId()).stream()
                .filter(p -> p.getStatus() == Project.Status.IN_PROGRESS
                        || p.getStatus() == Project.Status.OPEN_FOR_BIDS)
                .map(this::toContractorProject)
                .collect(Collectors.toList());
    }

    /** Earnings chart data: week = last 7 days, month = last 30 days */
    public MiscDto.EarningsResponse getEarnings(String period) {
        User owner = userService.getCurrentUser();
        LocalDate now = LocalDate.now();
        LocalDate from = "month".equalsIgnoreCase(period) ? now.minusDays(29) : now.minusDays(6);

        List<Earning> earnings = earningRepository
                .findByOwnerIdAndDateBetweenOrderByDateAsc(owner.getId(), from, now);

        // Build day-by-day map
        Map<LocalDate, Long> dayMap = new LinkedHashMap<>();
        for (LocalDate d = from; !d.isAfter(now); d = d.plusDays(1)) {
            dayMap.put(d, 0L);
        }
        for (Earning e : earnings) {
            dayMap.merge(e.getDate(), e.getAmount(), Long::sum);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE");
        List<MiscDto.EarningDataPoint> data = dayMap.entrySet().stream()
                .map(entry -> {
                    MiscDto.EarningDataPoint pt = new MiscDto.EarningDataPoint();
                    pt.setLabel(entry.getKey().format(fmt));
                    pt.setAmount(entry.getValue());
                    return pt;
                }).collect(Collectors.toList());

        long total = data.stream().mapToLong(MiscDto.EarningDataPoint::getAmount).sum();
        long peak = data.stream().mapToLong(MiscDto.EarningDataPoint::getAmount).max().orElse(0);
        long avg = data.isEmpty() ? 0 : total / data.size();

        MiscDto.EarningsResponse res = new MiscDto.EarningsResponse();
        res.setData(data);
        res.setTotal(total);
        res.setAverage(avg);
        res.setPeak(peak);
        return res;
    }

    // ── Schedule ───────────────────────────────────────────────────────────
    public List<MiscDto.ScheduleEventResponse> getSchedule() {
        User owner = userService.getCurrentUser();
        // Get all project IDs belonging to this owner
        Set<Long> myProjectIds = projectRepository.findByOwnerId(owner.getId())
                .stream().map(Project::getId).collect(Collectors.toSet());
        return scheduleEventRepository
                .findByDateGreaterThanEqualOrderByDateAsc(LocalDate.now()).stream()
                .filter(e -> myProjectIds.contains(e.getProjectId()))
                .map(this::toScheduleResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MiscDto.ScheduleEventResponse createScheduleEvent(MiscDto.ScheduleEventRequest req) {
        ScheduleEvent event = ScheduleEvent.builder()
                .workerId(req.getWorkerId()).projectId(req.getProjectId())
                .date(req.getDate()).startTime(req.getStartTime()).endTime(req.getEndTime())
                .build();
        return toScheduleResponse(scheduleEventRepository.save(event));
    }

    @Transactional
    public MiscDto.ScheduleEventResponse updateScheduleEvent(Long id, MiscDto.ScheduleEventRequest req) {
        ScheduleEvent e = scheduleEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule event not found"));
        if (req.getDate() != null)
            e.setDate(req.getDate());
        if (req.getStartTime() != null)
            e.setStartTime(req.getStartTime());
        if (req.getEndTime() != null)
            e.setEndTime(req.getEndTime());
        return toScheduleResponse(scheduleEventRepository.save(e));
    }

    @Transactional
    public void deleteScheduleEvent(Long id) {
        scheduleEventRepository.deleteById(id);
    }

    // ── Mappers ────────────────────────────────────────────────────────────
    private MiscDto.ContractorProjectResponse toContractorProject(Project p) {
        MiscDto.ContractorProjectResponse r = new MiscDto.ContractorProjectResponse();
        r.setId(p.getId());
        r.setName(p.getTitle());
        r.setLocation(p.getLocation());
        r.setProgress(p.getProgress());
        r.setDeadline(p.getDeadline() != null ? p.getDeadline().toString() : "");
        // Workers assigned = count of accepted bids
        long assigned = bidRepository.findByProjectId(p.getId()).stream()
                .filter(b -> b.getStatus() == Bid.Status.ACCEPTED).count();
        r.setWorkersAssigned((int) assigned);
        r.setStatus(p.getStatus().name());
        return r;
    }

    private MiscDto.ScheduleEventResponse toScheduleResponse(ScheduleEvent e) {
        MiscDto.ScheduleEventResponse r = new MiscDto.ScheduleEventResponse();
        r.setId(e.getId());
        r.setWorkerId(e.getWorkerId());
        r.setProjectId(e.getProjectId());
        r.setDate(e.getDate() != null ? e.getDate().toString() : "");
        r.setStartTime(e.getStartTime() != null ? e.getStartTime().toString() : "");
        r.setEndTime(e.getEndTime() != null ? e.getEndTime().toString() : "");
        userRepository.findById(e.getWorkerId()).ifPresent(u -> r.setWorkerName(u.getName()));
        projectRepository.findById(e.getProjectId()).ifPresent(p -> r.setProjectName(p.getTitle()));
        return r;
    }
}
