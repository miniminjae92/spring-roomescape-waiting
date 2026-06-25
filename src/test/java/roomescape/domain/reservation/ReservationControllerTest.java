package roomescape.domain.reservation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.LoginMember;
import roomescape.auth.SessionManager;
import roomescape.domain.member.MemberRole;
import roomescape.domain.reservation.dto.ReservationCreationRequest;
import roomescape.domain.reservation.dto.ReservationCreationResponse;
import roomescape.domain.reservation.dto.ReservationResponse;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        when(sessionManager.findLoginMember(any()))
            .thenReturn(Optional.of(new LoginMember(10L, MemberRole.USER)));
    }

    @Test
    void 로그인_회원의_예약_생성_요청을_처리한다() throws Exception {
        when(reservationService.createReservation(eq(10L), any(ReservationCreationRequest.class)))
            .thenReturn(new ReservationCreationResponse(
                1L,
                "고래",
                LocalDate.of(2026, 7, 1),
                LocalTime.of(10, 0),
                new ReservationCreationResponse.ThemePayload("공포", "설명", "/themes/scary")
            ));

        mockMvc.perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"dateId":1,"timeId":2,"themeId":3}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("고래"));

        verify(reservationService).createReservation(eq(10L), any(ReservationCreationRequest.class));
    }

    @Test
    void 로그인_회원의_예약만_조회한다() throws Exception {
        when(reservationService.getReservationsByMember(10L))
            .thenReturn(List.of(new ReservationResponse(
                1L,
                "고래",
                LocalDate.of(2026, 7, 1),
                new ReservationResponse.ReservationTimePayload(2L, LocalTime.of(10, 0)),
                new ReservationResponse.ThemePayload(3L, "공포", "설명", "/themes/scary")
            )));

        mockMvc.perform(get("/reservations"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1));

        verify(reservationService).getReservationsByMember(10L);
    }

    @Test
    void 로그인하지_않으면_예약_API를_사용할_수_없다() throws Exception {
        when(sessionManager.findLoginMember(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/reservations"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 예약_취소에_로그인_회원_ID를_전달한다() throws Exception {
        mockMvc.perform(delete("/reservations/1"))
            .andExpect(status().isNoContent());

        verify(reservationService).cancelReservation(10L, 1L);
    }
}
