package roomescape.domain.waitingreservation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.LoginMember;
import roomescape.auth.SessionManager;
import roomescape.domain.member.MemberRole;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRankResponse;

@WebMvcTest(AdminWaitingReservationController.class)
class AdminWaitingReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WaitingReservationService waitingReservationService;

    @MockitoBean
    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        when(sessionManager.findLoginMember(org.mockito.ArgumentMatchers.any()))
            .thenReturn(Optional.of(new LoginMember(1L, MemberRole.MANAGER)));
    }

    @Test
    void 관리자가_전체_예약_대기와_순위를_조회한다() throws Exception {
        when(waitingReservationService.getAllWaitingReservationsWithRank())
            .thenReturn(List.of(new WaitingReservationWithRankResponse(
                1L,
                "고래",
                LocalDate.of(2026, 7, 1),
                new ReservationResponse.ReservationTimePayload(2L, LocalTime.of(10, 0)),
                new ReservationResponse.ThemePayload(3L, "공포", "설명", "/themes/scary"),
                2L,
                LocalDateTime.of(2026, 6, 25, 10, 0)
            )));

        mockMvc.perform(get("/admin/waiting-reservations"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("고래"))
            .andExpect(jsonPath("$[0].rank").value(2));

        verify(waitingReservationService).getAllWaitingReservationsWithRank();
    }

    @Test
    void 관리자가_예약_대기를_취소한다() throws Exception {
        mockMvc.perform(delete("/admin/waiting-reservations/1"))
            .andExpect(status().isNoContent());

        verify(waitingReservationService).cancelWaitingReservationByAdmin(1L);
    }
}
