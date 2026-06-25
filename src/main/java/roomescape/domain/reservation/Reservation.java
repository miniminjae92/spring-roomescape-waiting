package roomescape.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.member.Member;
import roomescape.support.exception.ReservationErrorCode;
import roomescape.support.exception.ReservationTimeErrorCode;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.ThemeErrorCode;

@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"date_id", "time_id", "theme_id", "active_slot"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

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
    private ReservationStatus status;

    private Boolean activeSlot;
    private LocalDateTime createdAt;
    private LocalDateTime canceledAt;

    private Reservation(
        Long id,
        String name,
        Member member,
        ReservationDate date,
        ReservationTime time,
        Theme theme,
        ReservationStatus status,
        LocalDateTime createdAt,
        LocalDateTime canceledAt
    ) {
        validate(name, member, date, time, theme, status, createdAt);
        this.id = id;
        this.name = name;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.activeSlot = status.occupiesSlot() ? Boolean.TRUE : null;
        this.createdAt = createdAt;
        this.canceledAt = canceledAt;
    }

    private Reservation(
        String name,
        Member member,
        ReservationDate date,
        ReservationTime time,
        Theme theme,
        LocalDateTime createdAt
    ) {
        this(null, name, member, date, time, theme, ReservationStatus.CONFIRMED, createdAt, null);
    }

    public static Reservation createWithoutId(
        String name,
        Member member,
        ReservationDate date,
        ReservationTime time,
        Theme theme,
        LocalDateTime createdAt
    ) {
        return new Reservation(
            name,
            member,
            date,
            time,
            theme,
            createdAt
        );
    }

    public static Reservation of(
        Long id,
        String name,
        Member member,
        ReservationDate date,
        ReservationTime time,
        Theme theme,
        ReservationStatus status,
        LocalDateTime createdAt,
        LocalDateTime canceledAt
    ) {
        return new Reservation(
            id,
            name,
            member,
            date,
            time,
            theme,
            status,
            createdAt,
            canceledAt
        );
    }

    public void changeSlot(ReservationDate date, ReservationTime time) {
        if (date == null) {
            throw new RoomescapeException(ReservationErrorCode.INVALID_RESERVATION_DATE);
        }
        if (time == null) {
            throw new RoomescapeException(ReservationTimeErrorCode.INVALID_RESERVATION_TIME);
        }
        this.date = date;
        this.time = time;
    }

    public void cancel(LocalDateTime canceledAt) {
        if (!status.occupiesSlot()) {
            throw new RoomescapeException(ReservationErrorCode.RESERVATION_CANNOT_CANCEL);
        }
        this.status = ReservationStatus.CANCELED;
        this.activeSlot = null;
        this.canceledAt = canceledAt;
    }

    public boolean isOwnedBy(Long memberId) {
        return member.getId().equals(memberId);
    }

    private static void validate(
        String name,
        Member member,
        ReservationDate date,
        ReservationTime time,
        Theme theme,
        ReservationStatus status,
        LocalDateTime createdAt
    ) {
        if (name == null || name.isBlank()) {
            throw new RoomescapeException(ReservationErrorCode.INVALID_RESERVATION_NAME);
        }
        if (member == null) {
            throw new RoomescapeException(ReservationErrorCode.INVALID_RESERVATION_MEMBER);
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
        if (status == null || createdAt == null) {
            throw new RoomescapeException(ReservationErrorCode.INVALID_RESERVATION);
        }
    }
}
