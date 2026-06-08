package roomescape.domain.reservationtime.dto;

import java.time.LocalTime;
import roomescape.domain.reservationtime.ReservationTime;

public record ReservationTimeAvailabilityResult(
    Long timeId,
    LocalTime startAt,
    boolean available
) {

    public static ReservationTimeAvailabilityResult of(ReservationTime reservationTime, boolean available) {
        return new ReservationTimeAvailabilityResult(
            reservationTime.getId(),
            reservationTime.getStartAt(),
            available
        );
    }
}
