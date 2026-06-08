package roomescape.domain.waitingreservation;

import java.time.LocalDateTime;
import lombok.Getter;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.WaitingReservationErrorCode;

@Getter
public class WaitingReservation {

    private final Long id;
    private final String name;
    private final ReservationDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final LocalDateTime createdAt;
    private final WaitingReservationStatus status;
    private final Long promotedReservationId;

    private WaitingReservation(Long id, String name, ReservationDate date, ReservationTime time, Theme theme,
        LocalDateTime createdAt,
        WaitingReservationStatus status,
        Long promotedReservationId) {
        validate(name, createdAt);
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.createdAt = createdAt;
        this.status = status;
        this.promotedReservationId = promotedReservationId;
    }

    public static WaitingReservation createWithoutId(String name, ReservationDate date, ReservationTime time, Theme theme, LocalDateTime createdAt) {
        return new WaitingReservation(
            null, name, date, time, theme, createdAt, WaitingReservationStatus.WAITING, null
        );
    }

    public static WaitingReservation of(Long id, String name, ReservationDate date, ReservationTime time, Theme theme,
        LocalDateTime createdAt) {
        return of(id, name, date, time, theme, createdAt, WaitingReservationStatus.WAITING, null);
    }

    public static WaitingReservation of(
        Long id,
        String name,
        ReservationDate date,
        ReservationTime time,
        Theme theme,
        LocalDateTime createdAt,
        WaitingReservationStatus status,
        Long promotedReservationId
    ) {
        return new WaitingReservation(
            id, name, date, time, theme, createdAt, status, promotedReservationId
        );
    }

    public WaitingReservation cancel() {
        validateWaiting();
        return new WaitingReservation(
            id, name, date, time, theme, createdAt, WaitingReservationStatus.CANCELLED, null
        );
    }

    public WaitingReservation promote(Long reservationId) {
        validateWaiting();
        return new WaitingReservation(
            id, name, date, time, theme, createdAt, WaitingReservationStatus.PROMOTED, reservationId
        );
    }

    private void validateWaiting() {
        if (status != WaitingReservationStatus.WAITING) {
            throw new RoomescapeException(WaitingReservationErrorCode.WAITING_RESERVATION_ALREADY_PROCESSED);
        }
    }

    private static void validate(String name, LocalDateTime createdAt) {
        if (name == null || name.isBlank()) {
            throw new RoomescapeException(WaitingReservationErrorCode.INVALID_RESERVATION_NAME);
        }

        if (createdAt == null) {
            throw new RoomescapeException(WaitingReservationErrorCode.INVALID_CREATED_AT);
        }
    }
}
