package roomescape.domain.reservationtime.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
public record ReservationTimeAvailabilityResponse(
    Long timeId,
    @JsonFormat(pattern = "HH:mm")
    LocalTime startAt,
    boolean available
) {

    public static ReservationTimeAvailabilityResponse from(ReservationTimeAvailabilityResult reservationTime) {
        return new ReservationTimeAvailabilityResponse(
            reservationTime.timeId(),
            reservationTime.startAt(),
            reservationTime.available()
        );
    }
}
