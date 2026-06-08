package roomescape.domain.waitingreservation;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.waitingreservation.dto.AdminWaitingReservationResponse;

@RestController
@RequestMapping("/admin/waiting-reservations")
@RequiredArgsConstructor
public class AdminWaitingReservationController {

    private final WaitingReservationService waitingReservationService;

    @GetMapping
    public ResponseEntity<List<AdminWaitingReservationResponse>> getAll() {
        List<AdminWaitingReservationResponse> response = waitingReservationService.getAllWaitingReservations()
            .stream()
            .map(AdminWaitingReservationResponse::from)
            .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        waitingReservationService.cancelWaitingReservationByAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        waitingReservationService.deleteWaitingReservation(id);
        return ResponseEntity.noContent().build();
    }
}
