package roomescape.domain.reservation;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.Authenticated;
import roomescape.auth.LoginMember;
import roomescape.auth.LoginRequired;
import roomescape.domain.reservation.dto.ReservationCreationRequest;
import roomescape.domain.reservation.dto.ReservationCreationResponse;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.reservation.dto.ReservationUpdateRequest;

@RestController
@RequiredArgsConstructor
@LoginRequired
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/reservations")
    public ResponseEntity<ReservationCreationResponse> createReservation(
        @Authenticated LoginMember loginMember,
        @Valid @RequestBody ReservationCreationRequest request) {
        ReservationCreationResponse response = reservationService.createReservation(loginMember.memberId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservations(
        @Authenticated LoginMember loginMember
    ) {
        List<ReservationResponse> response = reservationService.getReservationsByMember(loginMember.memberId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> cancelReservation(
        @Authenticated LoginMember loginMember,
        @PathVariable Long id
    ) {
        reservationService.cancelReservation(loginMember.memberId(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reservations/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
        @Authenticated LoginMember loginMember,
        @PathVariable Long id,
        @Valid @RequestBody ReservationUpdateRequest request
    ) {
        ReservationResponse response = reservationService.updateReservation(loginMember.memberId(), id, request);
        return ResponseEntity.ok(response);
    }
}
