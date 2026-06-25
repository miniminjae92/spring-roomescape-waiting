package roomescape.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.exception.ReservationErrorCode;
import roomescape.support.exception.RoomescapeException;

@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"date_id", "time_id", "theme_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "date_id")
    private ReservationDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @Enumerated(EnumType.STRING)
    private ReservationSlotStatus status;

    private long price;

    @Version
    private long version;

    private ReservationSlot(
        Long id,
        ReservationDate date,
        ReservationTime time,
        Theme theme,
        ReservationSlotStatus status,
        long price
    ) {
        validate(date, time, theme, status, price);
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.price = price;
    }

    public static ReservationSlot createWithoutId(
        ReservationDate date,
        ReservationTime time,
        Theme theme,
        long price
    ) {
        return new ReservationSlot(null, date, time, theme, ReservationSlotStatus.OPEN, price);
    }

    public static ReservationSlot of(
        Long id,
        ReservationDate date,
        ReservationTime time,
        Theme theme,
        ReservationSlotStatus status,
        long price
    ) {
        return new ReservationSlot(id, date, time, theme, status, price);
    }

    public static ReservationSlot from(Reservation reservation) {
        return reservation.getSlot();
    }

    public Long dateId() {
        return date.getId();
    }

    public Long timeId() {
        return time.getId();
    }

    public Long themeId() {
        return theme.getId();
    }

    public boolean isSameSlot(ReservationSlot other) {
        return id != null && id.equals(other.id);
    }

    public boolean isClosedForReservation(Clock clock) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date.getPlayDay(), time.getStartAt());
        LocalDateTime deadline = reservationDateTime.minus(Duration.ofMinutes(10));
        LocalDateTime now = LocalDateTime.now(clock);
        return status == ReservationSlotStatus.CLOSED || !now.isBefore(deadline);
    }

    public void close() {
        status = ReservationSlotStatus.CLOSED;
    }

    public void open() {
        status = ReservationSlotStatus.OPEN;
    }

    private static void validate(
        ReservationDate date,
        ReservationTime time,
        Theme theme,
        ReservationSlotStatus status,
        long price
    ) {
        if (date == null || time == null || theme == null || status == null || price <= 0) {
            throw new RoomescapeException(ReservationErrorCode.INVALID_RESERVATION_SLOT);
        }
    }
}
