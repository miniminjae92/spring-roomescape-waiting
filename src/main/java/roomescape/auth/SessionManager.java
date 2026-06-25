package roomescape.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class SessionManager {

    public static final String COOKIE_NAME = "ROOMESCAPE_SESSION";
    public static final String LOGIN_MEMBER_ATTRIBUTE = "loginMember";

    private final SessionStore sessionStore;
    private final Duration sessionTtl;
    private final boolean secureCookie;

    public SessionManager(
        SessionStore sessionStore,
        @Value("${auth.session.ttl:PT2H}") Duration sessionTtl,
        @Value("${auth.session.secure-cookie:false}") boolean secureCookie
    ) {
        this.sessionStore = sessionStore;
        this.sessionTtl = sessionTtl;
        this.secureCookie = secureCookie;
    }

    public String create(LoginMember loginMember) {
        return sessionStore.create(loginMember, sessionTtl);
    }

    public Optional<LoginMember> findLoginMember(HttpServletRequest request) {
        return findSessionId(request).flatMap(sessionStore::find);
    }

    public void invalidate(HttpServletRequest request) {
        findSessionId(request).ifPresent(sessionStore::delete);
    }

    public String createCookie(String sessionId) {
        return cookie(sessionId)
            .maxAge(sessionTtl)
            .build()
            .toString();
    }

    public String expireCookie() {
        return cookie("")
            .maxAge(Duration.ZERO)
            .build()
            .toString();
    }

    private ResponseCookie.ResponseCookieBuilder cookie(String value) {
        return ResponseCookie.from(COOKIE_NAME, value)
            .httpOnly(true)
            .secure(secureCookie)
            .sameSite("Lax")
            .path("/");
    }

    private Optional<String> findSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
            .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
            .map(Cookie::getValue)
            .filter(value -> !value.isBlank())
            .findFirst();
    }
}
