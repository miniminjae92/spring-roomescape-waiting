package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.support.exception.AuthErrorCode;
import roomescape.support.exception.RoomescapeException;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final SessionManager sessionManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        LoginRequired loginRequired = findLoginRequired(handlerMethod);
        if (loginRequired == null) {
            return true;
        }
        LoginMember loginMember = sessionManager.findLoginMember(request)
            .orElseThrow(() -> new RoomescapeException(AuthErrorCode.AUTHENTICATION_REQUIRED));
        if (loginRequired.managerOnly() && !loginMember.isManager()) {
            throw new RoomescapeException(AuthErrorCode.AUTHORIZATION_FAILED);
        }
        request.setAttribute(SessionManager.LOGIN_MEMBER_ATTRIBUTE, loginMember);
        return true;
    }

    private LoginRequired findLoginRequired(HandlerMethod handlerMethod) {
        LoginRequired methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequired.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return handlerMethod.getBeanType().getAnnotation(LoginRequired.class);
    }
}
