package roomescape.domain.reservationdate.dto;

import java.time.LocalDate;
public record ReservationDateCreationResponse(
    Long id,
    LocalDate playDay
) {

    public static ReservationDateCreationResponse from(ReservationDateResult reservationDate) {
        return new ReservationDateCreationResponse(reservationDate.id(), reservationDate.playDay());
    }
}
