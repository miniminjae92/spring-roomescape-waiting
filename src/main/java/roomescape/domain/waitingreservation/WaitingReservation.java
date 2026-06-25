package roomescape.domain.waitingreservation;

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
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.WaitingReservationErrorCode;

@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "date_id", "time_id", "theme_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WaitingReservation {

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

    private LocalDateTime createdAt;
    private LocalDateTime canceledAt;

    @Enumerated(EnumType.STRING)
    private WaitingReservationStatus status;

    private WaitingReservation(
        Long id,
        String name,
        Member member,
        ReservationDate date,
        ReservationTime time,
        Theme theme,
        LocalDateTime createdAt,
        LocalDateTime canceledAt,
        WaitingReservationStatus status
    ) {
        validate(name, member, createdAt, status);
        this.id = id;
        this.name = name;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.createdAt = createdAt;
        this.canceledAt = canceledAt;
        this.status = status;
    }

    public static WaitingReservation createWithoutId(
        String name,
        Member member,
        ReservationDate date,
        ReservationTime time,
        Theme theme,
        LocalDateTime createdAt
    ) {
        return new WaitingReservation(
            null,
            name,
            member,
            date,
            time,
            theme,
            createdAt,
            null,
            WaitingReservationStatus.WAITING
        );
    }

    public static WaitingReservation of(
        Long id,
        String name,
        Member member,
        ReservationDate date,
        ReservationTime time,
        Theme theme,
        LocalDateTime createdAt,
        LocalDateTime canceledAt,
        WaitingReservationStatus status
    ) {
        return new WaitingReservation(id, name, member, date, time, theme, createdAt, canceledAt, status);
    }

    public void cancel(LocalDateTime canceledAt) {
        if (status != WaitingReservationStatus.WAITING) {
            throw new RoomescapeException(WaitingReservationErrorCode.WAITING_RESERVATION_CANNOT_CANCEL);
        }
        this.status = WaitingReservationStatus.CANCELED;
        this.canceledAt = canceledAt;
    }

    public void convert() {
        if (status != WaitingReservationStatus.WAITING) {
            throw new RoomescapeException(WaitingReservationErrorCode.WAITING_RESERVATION_CANNOT_CONVERT);
        }
        this.status = WaitingReservationStatus.CONVERTED;
    }

    public boolean isOwnedBy(Long memberId) {
        return member.getId().equals(memberId);
    }

    private static void validate(
        String name,
        Member member,
        LocalDateTime createdAt,
        WaitingReservationStatus status
    ) {
        if (name == null || name.isBlank()) {
            throw new RoomescapeException(WaitingReservationErrorCode.INVALID_RESERVATION_NAME);
        }

        if (createdAt == null) {
            throw new RoomescapeException(WaitingReservationErrorCode.INVALID_CREATED_AT);
        }
        if (member == null || status == null) {
            throw new RoomescapeException(WaitingReservationErrorCode.INVALID_WAITING_RESERVATION);
        }
    }
}
