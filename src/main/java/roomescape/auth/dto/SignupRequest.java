package roomescape.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
    @NotBlank(message = "로그인 ID는 필수입니다")
    @Size(min = 4, max = 30, message = "로그인 ID는 4자 이상 30자 이하여야 합니다")
    String loginId,

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다")
    String password,

    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 20, message = "이름은 20자 이하여야 합니다")
    String name
) {
}
