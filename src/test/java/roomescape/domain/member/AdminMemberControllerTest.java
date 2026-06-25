package roomescape.domain.member;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import roomescape.auth.dto.MemberResponse;

@WebMvcTest(AdminMemberController.class)
class AdminMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        when(sessionManager.findLoginMember(org.mockito.ArgumentMatchers.any()))
            .thenReturn(Optional.of(new LoginMember(1L, MemberRole.MANAGER)));
    }

    @Test
    void 관리자가_예약할_수_있는_회원_목록을_조회한다() throws Exception {
        when(memberService.getAllUsers())
            .thenReturn(List.of(new MemberResponse(2L, "user", "고래", MemberRole.USER)));

        mockMvc.perform(get("/admin/members"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(2))
            .andExpect(jsonPath("$[0].name").value("고래"));

        verify(memberService).getAllUsers();
    }
}
