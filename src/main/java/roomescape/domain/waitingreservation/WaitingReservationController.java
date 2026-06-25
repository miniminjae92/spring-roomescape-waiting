package roomescape.domain.waitingreservation;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.Authenticated;
import roomescape.auth.LoginMember;
import roomescape.auth.LoginRequired;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationRequest;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationResponse;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRankResponse;

@RestController
@RequestMapping("/waiting-reservations")
@RequiredArgsConstructor
@LoginRequired
public class WaitingReservationController {

    private final WaitingReservationService waitingReservationService;

    @PostMapping
    public ResponseEntity<WaitingReservationCreationResponse> createWaitingReservation(
        @Authenticated LoginMember loginMember,
        @Valid @RequestBody WaitingReservationCreationRequest request
    ) {
        WaitingReservationCreationResponse response = waitingReservationService.createWaitingReservation(
            loginMember.memberId(),
            request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelWaitingReservation(
        @Authenticated LoginMember loginMember,
        @PathVariable Long id
    ) {
        waitingReservationService.cancelWaitingReservation(loginMember.memberId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<WaitingReservationWithRankResponse>> getWaitingReservations(
        @Authenticated LoginMember loginMember
    ) {
        List<WaitingReservationWithRankResponse> response =
            waitingReservationService.getWaitingReservationsWithRankByMember(loginMember.memberId());
        return ResponseEntity.ok(response);
    }
}
