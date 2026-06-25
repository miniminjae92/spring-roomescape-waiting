package roomescape.domain.reservation;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginRequired;
import roomescape.domain.reservation.dto.AdminReservationCreationRequest;
import roomescape.domain.reservation.dto.ReservationCreationResponse;
import roomescape.domain.reservation.dto.ReservationResponse;

@RestController
@RequiredArgsConstructor
@LoginRequired(managerOnly = true)
public class AdminReservationController {

    private final ReservationService reservationService;

    @GetMapping("/admin/reservations")
    public ResponseEntity<List<ReservationResponse>> getAllReservation() {
        List<ReservationResponse> response = reservationService.getAllReservations();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationCreationResponse> createReservation(
        @Valid @RequestBody AdminReservationCreationRequest request
    ) {
        ReservationCreationResponse response = reservationService.createReservation(
            request.memberId(),
            request.toReservationCreationRequest()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/admin/reservations/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
