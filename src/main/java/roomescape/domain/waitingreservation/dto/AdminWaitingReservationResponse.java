package roomescape.domain.waitingreservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.waitingreservation.WaitingReservationStatus;

public record AdminWaitingReservationResponse(
    Long id,
    String name,
    LocalDate date,
    ReservationResponse.ReservationTimePayload time,
    ReservationResponse.ThemePayload theme,
    WaitingReservationStatus status,
    LocalDateTime createdAt,
    Long promotedReservationId
) {

    public static AdminWaitingReservationResponse from(WaitingReservationResult waiting) {
        return new AdminWaitingReservationResponse(
            waiting.id(),
            waiting.name(),
            waiting.date(),
            ReservationResponse.ReservationTimePayload.from(waiting.time()),
            ReservationResponse.ThemePayload.from(waiting.theme()),
            waiting.status(),
            waiting.createdAt(),
            waiting.promotedReservationId()
        );
    }
}
