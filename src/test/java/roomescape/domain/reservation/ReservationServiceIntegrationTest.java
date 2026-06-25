package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;

@SpringBootTest
@Sql("/truncate.sql")
class ReservationServiceIntegrationTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationDateRepository dateRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationSlotRepository slotRepository;

    private Member member;
    private ReservationDate date;
    private ReservationTime time;
    private Theme theme;
    private ReservationSlot slot;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.createUser(
            "user",
            "encoded",
            "고래",
            LocalDateTime.now()
        ));
        date = dateRepository.save(ReservationDate.createWithoutId(LocalDate.now().plusDays(2)));
        time = timeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        theme = themeRepository.save(Theme.createWithoutId("공포", "설명", "/themes/scary"));
        slot = slotRepository.save(ReservationSlot.createWithoutId(date, time, theme, theme.getPrice()));
    }

    @Test
    void 활성_예약은_DB_제약으로_동일_슬롯에_하나만_저장된다() {
        reservationRepository.saveAndFlush(reservation());

        assertThatThrownBy(() -> reservationRepository.saveAndFlush(reservation()))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 취소한_예약은_보존되고_같은_슬롯을_다시_예약할_수_있다() {
        Reservation canceled = reservationRepository.saveAndFlush(reservation());
        canceled.cancel(LocalDateTime.now());
        reservationRepository.saveAndFlush(canceled);

        Reservation nextReservation = reservationRepository.saveAndFlush(reservation());

        assertThat(reservationRepository.findById(canceled.getId())).isPresent();
        assertThat(canceled.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(nextReservation.getId()).isNotEqualTo(canceled.getId());
    }

    private Reservation reservation() {
        return Reservation.createWithoutId(
            member.getName(),
            member,
            slot,
            LocalDateTime.now()
        );
    }
}
