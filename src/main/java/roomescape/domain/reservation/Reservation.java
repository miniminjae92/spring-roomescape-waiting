package roomescape.domain.reservation;

import lombok.Getter;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.exception.ReservationErrorCode;
import roomescape.support.exception.ReservationTimeErrorCode;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.ThemeErrorCode;

@Getter
public class Reservation {

    private final Long id;
    private final String name;
    private final ReservationDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final ReservationStatus status;

    private Reservation(
        Long id,
        String name,
        ReservationDate date,
        ReservationTime time,
        Theme theme,
        ReservationStatus status
    ) {
        validate(name, date, time, theme);
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    private Reservation(String name, ReservationDate date, ReservationTime time, Theme theme) {
        this(null, name, date, time, theme, ReservationStatus.RESERVED);
    }

    public static Reservation createWithoutId(
        String name,
        ReservationDate date,
        ReservationTime time,
        Theme theme
    ) {
        return new Reservation(
            name,
            date,
            time,
            theme
        );
    }

    public static Reservation of(
        Long id,
        String name,
        ReservationDate date,
        ReservationTime time,
        Theme theme
    ) {
        return of(id, name, date, time, theme, ReservationStatus.RESERVED);
    }

    public static Reservation of(
        Long id,
        String name,
        ReservationDate date,
        ReservationTime time,
        Theme theme,
        ReservationStatus status
    ) {
        return new Reservation(id, name, date, time, theme, status);
    }

    public Reservation cancel() {
        if (status != ReservationStatus.RESERVED) {
            throw new RoomescapeException(ReservationErrorCode.RESERVATION_ALREADY_CANCELLED);
        }
        return new Reservation(id, name, date, time, theme, ReservationStatus.CANCELLED);
    }

    private static void validate(String name, ReservationDate date, ReservationTime time, Theme theme) {
        if (name == null || name.isBlank()) {
            throw new RoomescapeException(ReservationErrorCode.INVALID_RESERVATION_NAME);
        }
        if (date == null) {
            throw new RoomescapeException(ReservationErrorCode.INVALID_RESERVATION_DATE);
        }
        if (time == null) {
            throw new RoomescapeException(ReservationTimeErrorCode.INVALID_RESERVATION_TIME);
        }
        if (theme == null) {
            throw new RoomescapeException(ThemeErrorCode.INVALID_THEME);
        }
    }
}
