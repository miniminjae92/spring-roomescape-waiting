package roomescape.domain.reservation.dto;

import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public record ReservationResult(
    Long id,
    String name,
    LocalDate date,
    ReservationTime time,
    Theme theme,
    ReservationStatus status
) {

    public static ReservationResult from(Reservation reservation) {
        return new ReservationResult(
            reservation.getId(),
            reservation.getName(),
            reservation.getDate().getPlayDay(),
            reservation.getTime(),
            reservation.getTheme(),
            reservation.getStatus()
        );
    }
}
