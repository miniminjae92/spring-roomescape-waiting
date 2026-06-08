package roomescape.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.theme.Theme;

public record ReservationCreationResponse(
    Long id,
    String name,
    LocalDate date,
    @JsonFormat(pattern = "HH:mm")
    LocalTime time,
    ThemePayload theme
) {

    public static ReservationCreationResponse from(ReservationResult result) {
        return new ReservationCreationResponse(
            result.id(),
            result.name(),
            result.date(),
            result.time().getStartAt(),
            ThemePayload.from(result.theme())
        );
    }

    public record ThemePayload(
        String name,
        String content,
        String url
    ) {

        public static ThemePayload from(Theme theme) {
            return new ThemePayload(
                theme.getName(),
                theme.getContent(),
                theme.getUrl()
            );
        }
    }
}
