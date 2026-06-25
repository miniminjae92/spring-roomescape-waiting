package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.MemberRole;
import roomescape.domain.reservation.dto.ReservationCreationRequest;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.waitingreservation.WaitingReservationRepository;
import roomescape.support.exception.RoomescapeException;

class ReservationServiceTest {

    private static final Clock CLOCK = Clock.fixed(
        Instant.parse("2026-06-25T00:00:00Z"),
        ZoneId.of("Asia/Seoul")
    );

    private ReservationRepository reservationRepository;
    private MemberRepository memberRepository;
    private ReservationService reservationService;
    private Member member;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        memberRepository = mock(MemberRepository.class);
        ReservationDateRepository dateRepository = mock(ReservationDateRepository.class);
        ReservationTimeRepository timeRepository = mock(ReservationTimeRepository.class);
        ThemeRepository themeRepository = mock(ThemeRepository.class);
        ReservationSlotRepository slotRepository = mock(ReservationSlotRepository.class);
        WaitingReservationRepository waitingRepository = mock(WaitingReservationRepository.class);

        ReservationDate date = ReservationDate.of(1L, LocalDate.of(2026, 7, 1));
        ReservationTime time = ReservationTime.of(2L, LocalTime.of(10, 0));
        Theme theme = Theme.of(3L, "공포", "설명", "/themes/scary");
        ReservationSlot slot = ReservationSlot.of(
            100L, date, time, theme, ReservationSlotStatus.OPEN, 30_000L
        );
        member = Member.of(
            10L,
            "user",
            "encoded",
            "고래",
            MemberRole.USER,
            LocalDateTime.now(CLOCK)
        );

        when(memberRepository.findById(10L)).thenReturn(Optional.of(member));
        when(dateRepository.findById(1L)).thenReturn(Optional.of(date));
        when(timeRepository.findById(2L)).thenReturn(Optional.of(time));
        when(themeRepository.findById(3L)).thenReturn(Optional.of(theme));
        when(slotRepository.findByDateIdAndTimeIdAndThemeId(1L, 2L, 3L)).thenReturn(Optional.of(slot));
        when(reservationRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        reservationService = new ReservationService(
            reservationRepository,
            new ReservationSlotResolver(dateRepository, timeRepository, themeRepository, slotRepository),
            waitingRepository,
            memberRepository,
            CLOCK
        );
    }

    @Test
    void 로그인_회원의_정보로_예약을_생성한다() {
        reservationService.createReservation(10L, new ReservationCreationRequest(1L, 2L, 3L));

        verify(reservationRepository).saveAndFlush(org.mockito.ArgumentMatchers.argThat(
            reservation -> reservation.getMember().equals(member)
                && reservation.getName().equals("고래")
        ));
    }

    @Test
    void 활성_예약이_있는_슬롯에는_예약할_수_없다() {
        when(reservationRepository.existsBySlotIdAndActiveSlotTrue(100L))
            .thenReturn(true);

        assertThatThrownBy(
            () -> reservationService.createReservation(10L, new ReservationCreationRequest(1L, 2L, 3L))
        ).isInstanceOf(RoomescapeException.class);
    }

    @Test
    void 다른_회원은_예약을_취소할_수_없다() {
        Reservation reservation = Reservation.of(
            1L,
            member.getName(),
            member,
            ReservationSlot.of(
                100L,
                ReservationDate.of(1L, LocalDate.of(2026, 7, 1)),
                ReservationTime.of(2L, LocalTime.of(10, 0)),
                Theme.of(3L, "공포", "설명", "/themes/scary"),
                ReservationSlotStatus.OPEN,
                30_000L
            ),
            ReservationStatus.CONFIRMED,
            LocalDateTime.now(CLOCK),
            null
        );
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(999L, 1L))
            .isInstanceOf(RoomescapeException.class);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }
}
