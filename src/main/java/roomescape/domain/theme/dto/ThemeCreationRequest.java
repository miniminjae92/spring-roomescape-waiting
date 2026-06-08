package roomescape.domain.theme.dto;

import jakarta.validation.constraints.NotBlank;

public record ThemeCreationRequest(
    @NotBlank(message = "테마 제목은 필수입니다")
    String name,

    @NotBlank(message = "테마 설명은 필수입니다")
    String content,

    @NotBlank(message = "테마 포스터 url은 필수입니다")
    String url
) {

    public CreateThemeCommand toCommand() {
        return new CreateThemeCommand(name, content, url);
    }
}
