package roomescape.domain.reservationdate.dto;

import java.time.LocalDate;
public record AdminReservationDateResponse(
    Long id,
    LocalDate playDay
) {

    public static AdminReservationDateResponse from(ReservationDateResult reservationDate) {
        return new AdminReservationDateResponse(reservationDate.id(), reservationDate.playDay());
    }
}
