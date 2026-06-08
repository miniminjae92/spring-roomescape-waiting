package roomescape.domain.reservationdate.dto;

import java.time.LocalDate;
public record ReservationDateResponse(
    Long id,
    LocalDate playDay
) {

    public static ReservationDateResponse from(ReservationDateResult reservationDate) {
        return new ReservationDateResponse(
            reservationDate.id(),
            reservationDate.playDay()
        );
    }
}
