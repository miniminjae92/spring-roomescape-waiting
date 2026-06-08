package roomescape.domain.waitingreservation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.admin.AdminRequestValidator;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.waitingreservation.dto.WaitingReservationResult;

@WebMvcTest(AdminWaitingReservationController.class)
class AdminWaitingReservationControllerTest {

    private static final String ADMIN_HEADER = "X-ADMIN-TOKEN";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WaitingReservationService waitingReservationService;

    @MockitoBean
    private AdminRequestValidator adminRequestValidator;

    @Test
    void 관리자_예약_대기_목록_조회_요청을_처리한다() throws Exception {
        when(waitingReservationService.getAllWaitingReservations())
            .thenReturn(List.of(new WaitingReservationResult(
                1L,
                "고래",
                LocalDate.of(2026, 6, 10),
                ReservationTime.of(2L, LocalTime.of(10, 0)),
                Theme.of(3L, "공포", "설명", "/themes/scary"),
                LocalDateTime.of(2026, 6, 8, 10, 0),
                WaitingReservationStatus.WAITING,
                null,
                null
            )));

        mockMvc.perform(get("/admin/waiting-reservations")
                .header(ADMIN_HEADER, "token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].time.startAt").value("10:00"))
            .andExpect(jsonPath("$[0].theme.name").value("공포"))
            .andExpect(jsonPath("$[0].status").value("WAITING"));
    }

    @Test
    void 관리자_예약_대기_취소_요청을_처리한다() throws Exception {
        mockMvc.perform(post("/admin/waiting-reservations/1/cancel")
                .header(ADMIN_HEADER, "token"))
            .andExpect(status().isNoContent());

        verify(waitingReservationService).cancelWaitingReservationByAdmin(1L);
    }

    @Test
    void 관리자_예약_대기_삭제_요청을_처리한다() throws Exception {
        mockMvc.perform(delete("/admin/waiting-reservations/1")
                .header(ADMIN_HEADER, "token"))
            .andExpect(status().isNoContent());

        verify(waitingReservationService).deleteWaitingReservation(1L);
    }
}
