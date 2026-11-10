package com.fridegeshare.reservation;

import java.util.Map;

import com.fridegeshare.common.SessionUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ReservationApiController {
    private final ReservationService reservations;

    ReservationApiController(ReservationService reservations) { this.reservations = reservations; }

    @PostMapping("/api/posts/{postId}/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, Object> create(@AuthenticationPrincipal SessionUser user, @PathVariable long postId) {
        return Map.of("data", reservations.claim(user, postId));
    }

    @PatchMapping("/api/reservations/{reservationId}/cancel")
    Map<String, Object> cancel(@AuthenticationPrincipal SessionUser user, @PathVariable long reservationId) {
        return Map.of("data", reservations.cancel(user, reservationId));
    }

    @PatchMapping("/api/reservations/{reservationId}/complete")
    Map<String, Object> complete(@AuthenticationPrincipal SessionUser user, @PathVariable long reservationId) {
        return Map.of("data", reservations.complete(user, reservationId));
    }

    @GetMapping("/api/me/reservations")
    Map<String, Object> mine(@AuthenticationPrincipal SessionUser user) {
        return Map.of("data", reservations.mine(user));
    }
}
