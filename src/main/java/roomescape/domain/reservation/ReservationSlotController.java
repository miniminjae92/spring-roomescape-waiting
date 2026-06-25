package roomescape.domain.reservation;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginRequired;
import roomescape.domain.reservation.dto.ReservationSlotCreationRequest;
import roomescape.domain.reservation.dto.ReservationSlotResponse;

@RestController
@RequiredArgsConstructor
public class ReservationSlotController {

    private final ReservationSlotService slotService;

    @GetMapping("/reservation-slots")
    public ResponseEntity<List<ReservationSlotResponse>> findSlots(
        @RequestParam Long themeId,
        @RequestParam Long dateId
    ) {
        return ResponseEntity.ok(slotService.findByThemeAndDate(themeId, dateId));
    }

    @PostMapping("/admin/reservation-slots")
    @LoginRequired(managerOnly = true)
    public ResponseEntity<ReservationSlotResponse> create(
        @Valid @RequestBody ReservationSlotCreationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(slotService.create(request));
    }

    @PatchMapping("/admin/reservation-slots/{id}/close")
    @LoginRequired(managerOnly = true)
    public ResponseEntity<Void> close(@PathVariable Long id) {
        slotService.close(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin/reservation-slots/{id}/open")
    @LoginRequired(managerOnly = true)
    public ResponseEntity<Void> open(@PathVariable Long id) {
        slotService.open(id);
        return ResponseEntity.noContent().build();
    }
}
