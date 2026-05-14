package com.labourhand.controller;

import com.labourhand.dto.BidDto;
import com.labourhand.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    /** Bids for a specific project (owner view / compare bids) */
    @GetMapping("/api/projects/{projectId}/bids")
    public ResponseEntity<List<BidDto.Response>> getProjectBids(@PathVariable Long projectId) {
        return ResponseEntity.ok(bidService.getBidsForProject(projectId));
    }

    /** Worker's own bids */
    @GetMapping("/api/bids/my")
    public ResponseEntity<List<BidDto.Response>> getMyBids() {
        return ResponseEntity.ok(bidService.getMyBids());
    }

    @GetMapping("/api/bids/{id}")
    public ResponseEntity<BidDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bidService.getById(id));
    }

    /** ANY worker (independent or team) can bid */
    @PostMapping("/api/bids")
    public ResponseEntity<BidDto.Response> placeBid(@Valid @RequestBody BidDto.Request req) {
        return ResponseEntity.ok(bidService.placeBid(req));
    }

    @PutMapping("/api/bids/{id}")
    public ResponseEntity<BidDto.Response> updateBid(
            @PathVariable Long id, @Valid @RequestBody BidDto.Request req) {
        return ResponseEntity.ok(bidService.updateBid(id, req));
    }

    @DeleteMapping("/api/bids/{id}")
    public ResponseEntity<Void> withdrawBid(@PathVariable Long id) {
        bidService.withdrawBid(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/bids/{id}/accept")
    public ResponseEntity<BidDto.Response> acceptBid(@PathVariable Long id) {
        return ResponseEntity.ok(bidService.acceptBid(id));
    }

    @PutMapping("/api/bids/{id}/reject")
    public ResponseEntity<BidDto.Response> rejectBid(@PathVariable Long id) {
        return ResponseEntity.ok(bidService.rejectBid(id));
    }
}
