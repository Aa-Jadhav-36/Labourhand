package com.labourhand.service;

import com.labourhand.dto.PaymentDto;
import com.labourhand.model.Payment;
import com.labourhand.model.ProgressUpdate;
import com.labourhand.model.Project;
import com.labourhand.model.User;
import com.labourhand.model.Bid;
import com.labourhand.repository.BidRepository;
import com.labourhand.repository.PaymentRepository;
import com.labourhand.repository.ProgressUpdateRepository;
import com.labourhand.repository.ProjectRepository;
import com.labourhand.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ProjectRepository projectRepository;
    private final ProgressUpdateRepository progressUpdateRepository;
    private final UserRepository userRepository;
    private final BidRepository bidRepository;

    @Transactional
    public PaymentDto.Response processPayment(Long projectId, PaymentDto.Request request, Long employerId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOwnerId().equals(employerId)) {
            throw new RuntimeException("Only the project owner can make payments");
        }

        if (project.getAcceptedBidId() == null) {
            throw new RuntimeException("Project has no accepted bid");
        }

        Bid bid = bidRepository.findById(project.getAcceptedBidId())
                .orElseThrow(() -> new RuntimeException("Accepted bid not found"));

        if (bid.getAmountPaid() + request.getAmount() > bid.getAmount()) {
            throw new RuntimeException("Payment exceeds remaining balance");
        }

        ProgressUpdate update = null;
        if (request.getProgressUpdateId() != null) {
            update = progressUpdateRepository.findById(request.getProgressUpdateId())
                    .orElseThrow(() -> new RuntimeException("Progress update not found"));

            if (update.getPaymentDemanded() == null || !update.getPaymentDemanded()) {
                throw new RuntimeException("No payment was demanded for this update");
            }
        }

        // Create Payment record
        Payment payment = Payment.builder()
                .projectId(projectId)
                .progressUpdateId(update != null ? update.getId() : null)
                .payerId(employerId)
                .payeeId(bid.getWorkerId()) // always pay the worker who won the bid
                .amount(request.getAmount())
                .paymentMethod(Payment.Method.valueOf(request.getPaymentMethod()))
                .status(Payment.Status.COMPLETED) // Since this is a simulated/cash success flow
                .referenceId(request.getReferenceId())
                .paidAt(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);
        
        // Update bid amount paid
        bid.setAmountPaid(bid.getAmountPaid() + payment.getAmount().longValue());
        bidRepository.save(bid);
        
        return toDto(payment);
    }

    public java.util.List<PaymentDto.Response> getPaymentsByProjectId(Long projectId) {
        return paymentRepository.findByProjectId(projectId).stream()
                .map(this::toDto)
                .collect(java.util.stream.Collectors.toList());
    }

    private PaymentDto.Response toDto(Payment p) {
        PaymentDto.Response res = new PaymentDto.Response();
        res.setId(p.getId());
        res.setProjectId(p.getProjectId());
        res.setProgressUpdateId(p.getProgressUpdateId());
        res.setPayerId(p.getPayerId());
        res.setPayeeId(p.getPayeeId());
        res.setAmount(p.getAmount());
        res.setPaymentMethod(p.getPaymentMethod().name());
        res.setStatus(p.getStatus().name());
        res.setReferenceId(p.getReferenceId());
        res.setCreatedAt(p.getCreatedAt().toString());
        res.setPaidAt(p.getPaidAt() != null ? p.getPaidAt().toString() : null);

        // Fetch names if needed (optional for basic flow, we can leave null or fetch)
        userRepository.findById(p.getPayerId()).ifPresent(u -> res.setPayerName(u.getName()));
        userRepository.findById(p.getPayeeId()).ifPresent(u -> res.setPayeeName(u.getName()));

        return res;
    }
}
