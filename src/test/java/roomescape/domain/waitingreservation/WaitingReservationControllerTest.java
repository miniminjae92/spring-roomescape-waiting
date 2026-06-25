package roomescape.domain.waitingreservation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.LoginMember;
import roomescape.auth.SessionManager;
import roomescape.domain.member.MemberRole;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationRequest;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationResponse;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRankResponse;

@WebMvcTest(WaitingReservationController.class)
class WaitingReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WaitingReservationService waitingReservationService;

    @MockitoBean
    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        when(sessionManager.findLoginMember(any()))
            .thenReturn(Optional.of(new LoginMember(10L, MemberRole.USER)));
    }

    @Test
    void 로그인_회원의_예약_대기_생성_요청을_처리한다() throws Exception {
        when(waitingReservationService.createWaitingReservation(
            eq(10L),
            any(WaitingReservationCreationRequest.class)
        )).thenReturn(new WaitingReservationCreationResponse(
            1L,
            "고래",
            LocalDate.of(2026, 7, 1),
            LocalTime.of(10, 0),
            new WaitingReservationCreationResponse.ThemePayload("공포", "설명", "/themes/scary"),
            LocalDateTime.of(2026, 6, 25, 10, 0)
        ));

        mockMvc.perform(post("/waiting-reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"dateId":1,"timeId":2,"themeId":3}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("고래"));
    }

    @Test
    void 로그인_회원의_예약_대기만_조회한다() throws Exception {
        when(waitingReservationService.getWaitingReservationsWithRankByMember(10L))
            .thenReturn(List.of(new WaitingReservationWithRankResponse(
                1L,
                "고래",
                LocalDate.of(2026, 7, 1),
                new ReservationResponse.ReservationTimePayload(2L, LocalTime.of(10, 0)),
                new ReservationResponse.ThemePayload(3L, "공포", "설명", "/themes/scary"),
                1L,
                LocalDateTime.of(2026, 6, 25, 10, 0)
            )));

        mockMvc.perform(get("/waiting-reservations"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].rank").value(1));

        verify(waitingReservationService).getWaitingReservationsWithRankByMember(10L);
    }
}
