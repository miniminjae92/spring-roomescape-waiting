package roomescape.domain.reservationtime.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
public record ReservationTimeResponse(
    Long id,
    @JsonFormat(pattern = "HH:mm")
    LocalTime startAt
) {

    public static ReservationTimeResponse from(ReservationTimeResult reservationTime) {
        return new ReservationTimeResponse(
            reservationTime.id(),
            reservationTime.startAt()
        );
    }
}
