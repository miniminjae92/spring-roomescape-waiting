package roomescape.domain.waitingreservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.waitingreservation.WaitingReservation;

public record WaitingReservationCreationResponse(
    Long id,
    String name,
    LocalDate date,
    @JsonFormat(pattern = "HH:mm")
    LocalTime time,
    ThemePayload theme,
    LocalDateTime createdAt
) {

    public static WaitingReservationCreationResponse from(WaitingReservationResult result) {
        return new WaitingReservationCreationResponse(
            result.id(),
            result.name(),
            result.date(),
            result.time().getStartAt(),
            ThemePayload.from(result.theme()),
            result.createdAt()
        );
    }

    public record ThemePayload(String name, String content, String url) {

        private static ThemePayload from(Theme theme) {
            return new ThemePayload(theme.getName(), theme.getContent(), theme.getUrl());
        }
    }
}
