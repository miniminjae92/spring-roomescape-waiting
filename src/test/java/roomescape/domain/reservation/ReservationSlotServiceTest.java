package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.dto.ReservationSlotResponse;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;

class ReservationSlotServiceTest {

    private static final Clock CLOCK = Clock.fixed(
        Instant.parse("2026-06-25T00:00:00Z"),
        ZoneId.of("Asia/Seoul")
    );

    @Test
    void 예약_마감_시간이_지난_회차는_예약_불가로_응답한다() {
        ReservationSlotRepository slotRepository = mock(ReservationSlotRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        ReservationSlot slot = ReservationSlot.of(
            100L,
            ReservationDate.of(1L, LocalDate.of(2026, 6, 25)),
            ReservationTime.of(2L, LocalTime.of(9, 5)),
            Theme.of(3L, "공포", "설명", "/themes/scary"),
            ReservationSlotStatus.OPEN,
            30_000L
        );
        when(slotRepository.findAllByThemeIdAndDateIdOrderByTimeStartAt(3L, 1L))
            .thenReturn(List.of(slot));

        ReservationSlotService service = new ReservationSlotService(
            slotRepository,
            reservationRepository,
            mock(ReservationDateRepository.class),
            mock(ReservationTimeRepository.class),
            mock(ThemeRepository.class),
            CLOCK
        );

        List<ReservationSlotResponse> response = service.findByThemeAndDate(3L, 1L);

        assertThat(response).singleElement()
            .extracting(ReservationSlotResponse::available)
            .isEqualTo(false);
    }
}
