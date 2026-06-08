package roomescape.domain.waitingreservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.waitingreservation.WaitingReservation;
import roomescape.domain.waitingreservation.WaitingReservationStatus;

public record WaitingReservationResult(
    Long id,
    String name,
    LocalDate date,
    ReservationTime time,
    Theme theme,
    LocalDateTime createdAt,
    WaitingReservationStatus status,
    Long promotedReservationId,
    Long rank
) {

    public static WaitingReservationResult from(WaitingReservation waiting) {
        return from(waiting, null);
    }

    public static WaitingReservationResult from(WaitingReservation waiting, Long rank) {
        return new WaitingReservationResult(
            waiting.getId(),
            waiting.getName(),
            waiting.getDate().getPlayDay(),
            waiting.getTime(),
            waiting.getTheme(),
            waiting.getCreatedAt(),
            waiting.getStatus(),
            waiting.getPromotedReservationId(),
            rank
        );
    }
}
