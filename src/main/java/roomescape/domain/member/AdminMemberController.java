package roomescape.domain.member;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginRequired;
import roomescape.auth.dto.MemberResponse;

@RestController
@RequiredArgsConstructor
@LoginRequired(managerOnly = true)
public class AdminMemberController {

    private final MemberService memberService;

    @GetMapping("/admin/members")
    public ResponseEntity<List<MemberResponse>> getAllUsers() {
        return ResponseEntity.ok(memberService.getAllUsers());
    }
}
