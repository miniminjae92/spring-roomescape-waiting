package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.MemberResponse;
import roomescape.auth.dto.SignupRequest;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SessionManager sessionManager;

    @PostMapping("/signup")
    public ResponseEntity<MemberResponse> signup(@Valid @RequestBody SignupRequest request) {
        MemberResponse response = authService.signup(request);
        String sessionId = sessionManager.create(response.toLoginMember());
        return ResponseEntity.status(HttpStatus.CREATED)
            .header(HttpHeaders.SET_COOKIE, sessionManager.createCookie(sessionId))
            .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<MemberResponse> login(@Valid @RequestBody LoginRequest request) {
        MemberResponse response = authService.login(request);
        String sessionId = sessionManager.create(response.toLoginMember());
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, sessionManager.createCookie(sessionId))
            .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        sessionManager.invalidate(request);
        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, sessionManager.expireCookie())
            .build();
    }

    @GetMapping("/me")
    @LoginRequired
    public ResponseEntity<MemberResponse> me(@Authenticated LoginMember loginMember) {
        return ResponseEntity.ok(authService.getMember(loginMember.memberId()));
    }
}
