package roomescape.domain.reservationdate.dto;

import java.time.LocalDate;

public record CreateReservationDateCommand(
    LocalDate playDay
) {
}
