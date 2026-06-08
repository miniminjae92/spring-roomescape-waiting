package roomescape.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public record ReservationResponse(
    Long id,
    String name,
    LocalDate date,
    ReservationTimePayload time,
    ThemePayload theme,
    ReservationStatus status
) {

    public ReservationResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimePayload time,
        ThemePayload theme
    ) {
        this(id, name, date, time, theme, ReservationStatus.RESERVED);
    }

    public static ReservationResponse from(Reservation reservation) {
        return from(ReservationResult.from(reservation));
    }

    public static ReservationResponse from(ReservationResult result) {
        return new ReservationResponse(
            result.id(),
            result.name(),
            result.date(),
            ReservationTimePayload.from(result.time()),
            ThemePayload.from(result.theme()),
            result.status()
        );
    }

    public record ReservationTimePayload(
        Long id,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt
    ) {

        public static ReservationTimePayload from(ReservationTime reservationTime) {
            return new ReservationTimePayload(reservationTime.getId(), reservationTime.getStartAt());
        }
    }

    public record ThemePayload(
        Long id,
        String name,
        String content,
        String url
    ) {

        public static ThemePayload from(Theme theme) {
            return new ThemePayload(
                theme.getId(),
                theme.getName(),
                theme.getContent(),
                theme.getUrl()
            );
        }
    }
}
