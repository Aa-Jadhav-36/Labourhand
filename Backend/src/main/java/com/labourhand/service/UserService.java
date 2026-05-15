package com.labourhand.service;

import com.labourhand.dto.UserDto;
import com.labourhand.model.*;
import com.labourhand.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final OwnerProfileRepository ownerProfileRepository;

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserDto getCurrentUserDto() {
        User user = getCurrentUser();
        return toDto(user, workerProfileRepository, ownerProfileRepository);
    }

    public UserDto updateCurrentUser(UserDto updates) {
        User user = getCurrentUser();
        if (updates.getName() != null)
            user.setName(updates.getName());
        if (updates.getPhone() != null)
            user.setPhone(updates.getPhone());
        if (updates.getAvatar() != null)
            user.setAvatar(updates.getAvatar());
        if (updates.getLanguage() != null)
            user.setLanguage(User.Language.valueOf(updates.getLanguage()));
        userRepository.save(user);

        if (user.getRole() == User.Role.WORKER) {
            workerProfileRepository.findById(user.getId()).ifPresent(wp -> {
                if (updates.getSpecialization() != null)
                    wp.setSpecialization(updates.getSpecialization());
                if (updates.getBio() != null)
                    wp.setBio(updates.getBio());
                if (updates.getYearsExperience() > 0)
                    wp.setYearsExperience(updates.getYearsExperience());
                if (updates.getWorkerStatus() != null)
                    wp.setStatus(updates.getWorkerStatus());
                workerProfileRepository.save(wp);
            });
        } else {
            ownerProfileRepository.findById(user.getId()).ifPresent(op -> {
                if (updates.getCompanyName() != null)
                    op.setCompanyName(updates.getCompanyName());
                ownerProfileRepository.save(op);
            });
        }
        return getCurrentUserDto();
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> toDto(u, workerProfileRepository, ownerProfileRepository))
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        return toDto(user, workerProfileRepository, ownerProfileRepository);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // ── Static DTO mapper (used by AuthService + WorkerService) ─────────────
    public static UserDto toDto(User user,
            WorkerProfileRepository wpRepo,
            OwnerProfileRepository opRepo) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setAvatar(user.getAvatar());
        dto.setVerified(user.isVerified());
        dto.setLanguage(user.getLanguage() != null ? user.getLanguage().name() : "en");

        if (user.getRole() == User.Role.WORKER) {
            wpRepo.findById(user.getId()).ifPresent(wp -> {
                dto.setSpecialization(wp.getSpecialization());
                dto.setYearsExperience(wp.getYearsExperience());
                dto.setRating(wp.getRating());
                dto.setCompletedJobs(wp.getCompletedJobs());
                dto.setBio(wp.getBio());
                dto.setSkillsIndiaVerified(wp.isSkillsIndiaVerified());
                dto.setOnTimeRate(wp.getOnTimeRate());
                dto.setRehireRate(wp.getRehireRate());
                dto.setWorkerStatus(wp.getStatus());
                dto.setSkills(wp.getSkills().stream().map(Skill::getName).collect(Collectors.toList()));
                
                // Map Payment Details
                dto.setPaymentMethod(wp.getPaymentMethod());
                dto.setBankAccountNo(wp.getBankAccountNo());
                dto.setBankName(wp.getBankName());
                dto.setIfscCode(wp.getIfscCode());
                dto.setHolderName(wp.getHolderName());
                dto.setUpiId(wp.getUpiId());
            });
        } else {
            opRepo.findById(user.getId()).ifPresent(op -> {
                dto.setCompanyName(op.getCompanyName());
                dto.setProjectsPosted(op.getProjectsPosted());
            });
        }
        return dto;
    }
}
