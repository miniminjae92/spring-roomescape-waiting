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
import roomescape.domain.reservation.ReservationSlot;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.WaitingReservationErrorCode;

@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "slot_id", "status"}))
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
    @JoinColumn(name = "slot_id")
    private ReservationSlot slot;

    private LocalDateTime createdAt;
    private LocalDateTime canceledAt;

    @Enumerated(EnumType.STRING)
    private WaitingReservationStatus status;

    private WaitingReservation(
        Long id,
        String name,
        Member member,
        ReservationSlot slot,
        LocalDateTime createdAt,
        LocalDateTime canceledAt,
        WaitingReservationStatus status
    ) {
        validate(name, member, slot, createdAt, status);
        this.id = id;
        this.name = name;
        this.member = member;
        this.slot = slot;
        this.createdAt = createdAt;
        this.canceledAt = canceledAt;
        this.status = status;
    }

    public static WaitingReservation createWithoutId(
        String name,
        Member member,
        ReservationSlot slot,
        LocalDateTime createdAt
    ) {
        return new WaitingReservation(
            null,
            name,
            member,
            slot,
            createdAt,
            null,
            WaitingReservationStatus.WAITING
        );
    }

    public static WaitingReservation of(
        Long id,
        String name,
        Member member,
        ReservationSlot slot,
        LocalDateTime createdAt,
        LocalDateTime canceledAt,
        WaitingReservationStatus status
    ) {
        return new WaitingReservation(id, name, member, slot, createdAt, canceledAt, status);
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

    public ReservationDate getDate() {
        return slot.getDate();
    }

    public ReservationTime getTime() {
        return slot.getTime();
    }

    public Theme getTheme() {
        return slot.getTheme();
    }

    private static void validate(
        String name,
        Member member,
        ReservationSlot slot,
        LocalDateTime createdAt,
        WaitingReservationStatus status
    ) {
        if (name == null || name.isBlank()) {
            throw new RoomescapeException(WaitingReservationErrorCode.INVALID_RESERVATION_NAME);
        }

        if (createdAt == null) {
            throw new RoomescapeException(WaitingReservationErrorCode.INVALID_CREATED_AT);
        }
        if (member == null || slot == null || status == null) {
            throw new RoomescapeException(WaitingReservationErrorCode.INVALID_WAITING_RESERVATION);
        }
    }
}
