package roomescape.domain.waitingreservation;

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
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSlotResolver;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationSlotRepository;
import roomescape.domain.reservation.ReservationSlotStatus;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationRequest;
import roomescape.support.exception.RoomescapeException;

class WaitingReservationServiceTest {

    private static final Clock CLOCK = Clock.fixed(
        Instant.parse("2026-06-25T00:00:00Z"),
        ZoneId.of("Asia/Seoul")
    );

    private WaitingReservationRepository waitingRepository;
    private ReservationRepository reservationRepository;
    private WaitingReservationService waitingService;
    private Member member;

    @BeforeEach
    void setUp() {
        waitingRepository = mock(WaitingReservationRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        ReservationDateRepository dateRepository = mock(ReservationDateRepository.class);
        ReservationTimeRepository timeRepository = mock(ReservationTimeRepository.class);
        ThemeRepository themeRepository = mock(ThemeRepository.class);
        ReservationSlotRepository slotRepository = mock(ReservationSlotRepository.class);

        member = Member.of(
            10L,
            "user",
            "encoded",
            "고래",
            MemberRole.USER,
            LocalDateTime.now(CLOCK)
        );
        ReservationDate date = ReservationDate.of(1L, LocalDate.of(2026, 7, 1));
        ReservationTime time = ReservationTime.of(2L, LocalTime.of(10, 0));
        Theme theme = Theme.of(3L, "공포", "설명", "/themes/scary");
        ReservationSlot slot = ReservationSlot.of(
            100L, date, time, theme, ReservationSlotStatus.OPEN, 30_000L
        );

        when(memberRepository.findById(10L)).thenReturn(Optional.of(member));
        when(dateRepository.findById(1L)).thenReturn(Optional.of(date));
        when(timeRepository.findById(2L)).thenReturn(Optional.of(time));
        when(themeRepository.findById(3L)).thenReturn(Optional.of(theme));
        when(slotRepository.findByDateIdAndTimeIdAndThemeId(1L, 2L, 3L)).thenReturn(Optional.of(slot));
        when(reservationRepository.existsBySlotIdAndActiveSlotTrue(100L))
            .thenReturn(true);
        when(waitingRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        waitingService = new WaitingReservationService(
            waitingRepository,
            reservationRepository,
            new ReservationSlotResolver(dateRepository, timeRepository, themeRepository, slotRepository),
            memberRepository,
            CLOCK
        );
    }

    @Test
    void 로그인_회원의_정보로_예약_대기를_생성한다() {
        waitingService.createWaitingReservation(10L, new WaitingReservationCreationRequest(1L, 2L, 3L));

        verify(waitingRepository).saveAndFlush(org.mockito.ArgumentMatchers.argThat(
            waiting -> waiting.getMember().equals(member)
                && waiting.getName().equals("고래")
        ));
    }

    @Test
    void 같은_회원은_같은_슬롯에_중복_대기할_수_없다() {
        when(waitingRepository.existsByMemberIdAndSlotIdAndStatus(
            10L,
            100L,
            WaitingReservationStatus.WAITING
        )).thenReturn(true);

        assertThatThrownBy(
            () -> waitingService.createWaitingReservation(10L, new WaitingReservationCreationRequest(1L, 2L, 3L))
        ).isInstanceOf(RoomescapeException.class);
    }

    @Test
    void 다른_회원은_예약_대기를_취소할_수_없다() {
        WaitingReservation waiting = WaitingReservation.of(
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
            LocalDateTime.now(CLOCK),
            null,
            WaitingReservationStatus.WAITING
        );
        when(waitingRepository.findById(1L)).thenReturn(Optional.of(waiting));

        assertThatThrownBy(() -> waitingService.cancelWaitingReservation(999L, 1L))
            .isInstanceOf(RoomescapeException.class);
        assertThat(waiting.getStatus()).isEqualTo(WaitingReservationStatus.WAITING);
    }
}
