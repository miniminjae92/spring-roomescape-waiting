package roomescape.domain.waitingreservation;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginRequired;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRankResponse;

@RestController
@RequiredArgsConstructor
@LoginRequired(managerOnly = true)
public class AdminWaitingReservationController {

    private final WaitingReservationService waitingReservationService;

    @GetMapping("/admin/waiting-reservations")
    public ResponseEntity<List<WaitingReservationWithRankResponse>> getWaitingReservations() {
        return ResponseEntity.ok(waitingReservationService.getAllWaitingReservationsWithRank());
    }

    @DeleteMapping("/admin/waiting-reservations/{id}")
    public ResponseEntity<Void> cancelWaitingReservation(@PathVariable Long id) {
        waitingReservationService.cancelWaitingReservationByAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
