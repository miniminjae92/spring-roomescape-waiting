package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRole;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.exception.RoomescapeException;

class ReservationTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 6, 25, 10, 0);

    @Test
    void 예약은_생성될_때_슬롯을_점유한다() {
        Reservation reservation = reservation();

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(reservation.getActiveSlot()).isTrue();
    }

    @Test
    void 예약을_취소하면_이력을_남기고_슬롯을_해제한다() {
        Reservation reservation = reservation();
        LocalDateTime canceledAt = CREATED_AT.plusHours(1);

        reservation.cancel(canceledAt);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(reservation.getActiveSlot()).isNull();
        assertThat(reservation.getCanceledAt()).isEqualTo(canceledAt);
    }

    @Test
    void 이미_취소한_예약은_다시_취소할_수_없다() {
        Reservation reservation = reservation();
        reservation.cancel(CREATED_AT.plusHours(1));

        assertThatThrownBy(() -> reservation.cancel(CREATED_AT.plusHours(2)))
            .isInstanceOf(RoomescapeException.class);
    }

    @Test
    void 예약_소유자를_판별한다() {
        Reservation reservation = reservation();

        assertThat(reservation.isOwnedBy(1L)).isTrue();
        assertThat(reservation.isOwnedBy(2L)).isFalse();
    }

    private Reservation reservation() {
        Member member = Member.of(
            1L,
            "user",
            "encoded",
            "고래",
            MemberRole.USER,
            CREATED_AT
        );
        return Reservation.createWithoutId(
            member.getName(),
            member,
            ReservationDate.createWithoutId(LocalDate.of(2026, 7, 1)),
            ReservationTime.createWithoutId(LocalTime.of(10, 0)),
            Theme.createWithoutId("공포", "설명", "/themes/scary"),
            CREATED_AT
        );
    }
}
