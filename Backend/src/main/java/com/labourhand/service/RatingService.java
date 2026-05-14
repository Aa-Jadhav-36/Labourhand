package com.labourhand.service;

import com.labourhand.dto.RatingDto;
import com.labourhand.model.Rating;
import com.labourhand.model.WorkerProfile;
import com.labourhand.repository.RatingRepository;
import com.labourhand.repository.UserRepository;
import com.labourhand.repository.WorkerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public RatingDto.Response addRating(RatingDto.Request req, Long fromUserId) {
        Rating rating = Rating.builder()
                .projectId(req.getProjectId())
                .fromUserId(fromUserId)
                .toUserId(req.getToUserId())
                .rating(req.getRating())
                .comment(req.getComment())
                .build();
        
        rating = ratingRepository.save(rating);
        
        // Update Worker Profile overall rating
        updateWorkerOverallRating(req.getToUserId());
        
        return toDto(rating);
    }

    public List<RatingDto.Response> getRatingsForWorker(Long workerId) {
        return ratingRepository.findByToUserId(workerId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private void updateWorkerOverallRating(Long workerId) {
        List<Rating> allRatings = ratingRepository.findByToUserId(workerId);
        if (allRatings.isEmpty()) return;

        double avg = allRatings.stream().mapToDouble(Rating::getRating).average().orElse(0.0);
        
        workerProfileRepository.findById(workerId).ifPresent(wp -> {
            wp.setRating(avg);
            workerProfileRepository.save(wp);
        });
    }

    private RatingDto.Response toDto(Rating r) {
        RatingDto.Response res = new RatingDto.Response();
        res.setId(r.getId());
        res.setProjectId(r.getProjectId());
        res.setFromUserId(r.getFromUserId());
        res.setToUserId(r.getToUserId());
        res.setRating(r.getRating());
        res.setComment(r.getComment());
        res.setCreatedAt(r.getCreatedAt().toString());
        
        userRepository.findById(r.getFromUserId()).ifPresent(u -> res.setFromUserName(u.getName()));
        
        return res;
    }
}
