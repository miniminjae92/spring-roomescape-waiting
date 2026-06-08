package roomescape.domain.reservationdate.dto;

import java.time.LocalDate;
import roomescape.domain.reservationdate.ReservationDate;

public record ReservationDateResult(
    Long id,
    LocalDate playDay
) {

    public static ReservationDateResult from(ReservationDate reservationDate) {
        return new ReservationDateResult(reservationDate.getId(), reservationDate.getPlayDay());
    }
}
