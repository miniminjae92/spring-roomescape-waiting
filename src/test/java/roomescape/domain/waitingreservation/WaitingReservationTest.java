package roomescape.domain.waitingreservation;

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

class WaitingReservationTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 6, 25, 10, 0);

    @Test
    void 예약_대기를_취소해도_이력을_보존한다() {
        WaitingReservation waitingReservation = waitingReservation();
        LocalDateTime canceledAt = CREATED_AT.plusHours(1);

        waitingReservation.cancel(canceledAt);

        assertThat(waitingReservation.getStatus()).isEqualTo(WaitingReservationStatus.CANCELED);
        assertThat(waitingReservation.getCanceledAt()).isEqualTo(canceledAt);
    }

    @Test
    void 대기를_예약으로_전환한다() {
        WaitingReservation waitingReservation = waitingReservation();

        waitingReservation.convert();

        assertThat(waitingReservation.getStatus()).isEqualTo(WaitingReservationStatus.CONVERTED);
    }

    @Test
    void 이미_처리한_대기는_다시_전환할_수_없다() {
        WaitingReservation waitingReservation = waitingReservation();
        waitingReservation.convert();

        assertThatThrownBy(waitingReservation::convert)
            .isInstanceOf(RoomescapeException.class);
    }

    private WaitingReservation waitingReservation() {
        Member member = Member.of(1L, "user", "encoded", "고래", MemberRole.USER, CREATED_AT);
        return WaitingReservation.createWithoutId(
            member.getName(),
            member,
            ReservationDate.createWithoutId(LocalDate.of(2026, 7, 1)),
            ReservationTime.createWithoutId(LocalTime.of(10, 0)),
            Theme.createWithoutId("공포", "설명", "/themes/scary"),
            CREATED_AT
        );
    }
}
