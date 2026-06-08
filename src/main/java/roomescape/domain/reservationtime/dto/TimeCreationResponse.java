package roomescape.domain.reservationtime.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
public record TimeCreationResponse(
    Long id,
    @JsonFormat(pattern = "HH:mm")
    LocalTime startAt
) {

    public static TimeCreationResponse from(ReservationTimeResult reservationTime) {
        return new TimeCreationResponse(
            reservationTime.id(),
            reservationTime.startAt()
        );
    }
}
