package roomescape.domain.theme.dto;

import roomescape.domain.theme.Theme;

public record ThemeCreationResponse(
    Long id,
    String name,
    String content,
    String url,
    long price
) {

    public static ThemeCreationResponse from(Theme theme) {
        return new ThemeCreationResponse(
            theme.getId(),
            theme.getName(),
            theme.getContent(),
            theme.getUrl(),
            theme.getPrice()
        );
    }
}
