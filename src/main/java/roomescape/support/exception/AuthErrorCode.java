package roomescape.support.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements ErrorCode {
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED,
        "로그인 정보가 올바르지 않습니다.", "로그인 ID와 비밀번호를 다시 확인하십시오."),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED,
        "로그인이 필요한 요청입니다.", "로그인 후 다시 요청하십시오."),
    AUTHORIZATION_FAILED(HttpStatus.FORBIDDEN,
        "요청한 기능을 사용할 권한이 없습니다.", "현재 계정의 역할과 접근 권한을 확인하십시오.");

    private final HttpStatus httpStatus;
    private final String message;
    private final String action;

    AuthErrorCode(HttpStatus httpStatus, String message, String action) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.action = action;
    }

    @Override
    public String getCode() {
        return name();
    }
}
