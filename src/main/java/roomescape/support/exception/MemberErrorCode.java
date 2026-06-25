package roomescape.support.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MemberErrorCode implements ErrorCode {
    INVALID_LOGIN_ID(HttpStatus.BAD_REQUEST,
        "로그인 ID가 유효하지 않습니다.", "공백이 아닌 로그인 ID를 입력하십시오."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST,
        "비밀번호가 유효하지 않습니다.", "비밀번호 입력 규칙을 확인하십시오."),
    INVALID_MEMBER_NAME(HttpStatus.BAD_REQUEST,
        "회원 이름이 유효하지 않습니다.", "공백이 아닌 이름을 입력하십시오."),
    INVALID_MEMBER(HttpStatus.BAD_REQUEST,
        "회원 정보가 유효하지 않습니다.", "필수 회원 정보를 확인하십시오."),
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT,
        "이미 사용 중인 로그인 ID입니다.", "다른 로그인 ID를 사용하십시오."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,
        "회원을 찾을 수 없습니다.", "회원 식별자와 계정 상태를 확인하십시오.");

    private final HttpStatus httpStatus;
    private final String message;
    private final String action;

    MemberErrorCode(HttpStatus httpStatus, String message, String action) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.action = action;
    }

    @Override
    public String getCode() {
        return name();
    }
}
