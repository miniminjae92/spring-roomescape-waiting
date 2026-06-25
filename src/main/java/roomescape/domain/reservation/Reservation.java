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
import roomescape.domain.member.Member;
import roomescape.support.exception.ReservationErrorCode;
import roomescape.support.exception.RoomescapeException;

@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"slot_id", "active_slot"}))
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
    @JoinColumn(name = "slot_id")
    private ReservationSlot slot;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private Boolean activeSlot;
    private LocalDateTime createdAt;
    private LocalDateTime canceledAt;

    private Reservation(
        Long id,
        String name,
        Member member,
        ReservationSlot slot,
        ReservationStatus status,
        LocalDateTime createdAt,
        LocalDateTime canceledAt
    ) {
        validate(name, member, slot, status, createdAt);
        this.id = id;
        this.name = name;
        this.member = member;
        this.slot = slot;
        this.status = status;
        this.activeSlot = status.occupiesSlot() ? Boolean.TRUE : null;
        this.createdAt = createdAt;
        this.canceledAt = canceledAt;
    }

    private Reservation(
        String name,
        Member member,
        ReservationSlot slot,
        LocalDateTime createdAt
    ) {
        this(null, name, member, slot, ReservationStatus.CONFIRMED, createdAt, null);
    }

    public static Reservation createWithoutId(
        String name,
        Member member,
        ReservationSlot slot,
        LocalDateTime createdAt
    ) {
        return new Reservation(name, member, slot, createdAt);
    }

    public static Reservation of(
        Long id,
        String name,
        Member member,
        ReservationSlot slot,
        ReservationStatus status,
        LocalDateTime createdAt,
        LocalDateTime canceledAt
    ) {
        return new Reservation(
            id,
            name,
            member,
            slot,
            status,
            createdAt,
            canceledAt
        );
    }

    public void changeSlot(ReservationSlot slot) {
        if (slot == null) {
            throw new RoomescapeException(ReservationErrorCode.INVALID_RESERVATION_SLOT);
        }
        this.slot = slot;
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

    public roomescape.domain.reservationdate.ReservationDate getDate() {
        return slot.getDate();
    }

    public roomescape.domain.reservationtime.ReservationTime getTime() {
        return slot.getTime();
    }

    public roomescape.domain.theme.Theme getTheme() {
        return slot.getTheme();
    }

    private static void validate(
        String name,
        Member member,
        ReservationSlot slot,
        ReservationStatus status,
        LocalDateTime createdAt
    ) {
        if (name == null || name.isBlank()) {
            throw new RoomescapeException(ReservationErrorCode.INVALID_RESERVATION_NAME);
        }
        if (member == null) {
            throw new RoomescapeException(ReservationErrorCode.INVALID_RESERVATION_MEMBER);
        }
        if (slot == null) {
            throw new RoomescapeException(ReservationErrorCode.INVALID_RESERVATION_SLOT);
        }
        if (status == null || createdAt == null) {
            throw new RoomescapeException(ReservationErrorCode.INVALID_RESERVATION);
        }
    }
}
