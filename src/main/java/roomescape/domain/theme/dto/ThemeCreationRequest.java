package roomescape.domain.theme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import roomescape.domain.theme.Theme;

public record ThemeCreationRequest(
    @NotBlank(message = "테마 제목은 필수입니다")
    String name,

    @NotBlank(message = "테마 설명은 필수입니다")
    String content,

    @NotBlank(message = "테마 포스터 url은 필수입니다")
    String url,

    @Positive(message = "테마 가격은 0원보다 커야 합니다")
    Long price
) {

    public ThemeCreationRequest(String name, String content, String url) {
        this(name, content, url, 30_000L);
    }

    public Theme toEntity() {
        return Theme.createWithoutId(
            name,
            content,
            url,
            price == null ? 30_000L : price
        );
    }
}
