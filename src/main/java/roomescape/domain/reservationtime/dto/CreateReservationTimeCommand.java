package roomescape.domain.reservationtime.dto;

import java.time.LocalTime;

public record CreateReservationTimeCommand(
    LocalTime startAt
) {
}
