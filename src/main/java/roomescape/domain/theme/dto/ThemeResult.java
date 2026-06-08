package roomescape.domain.theme.dto;

import roomescape.domain.theme.Theme;

public record ThemeResult(
    Long id,
    String name,
    String content,
    String url
) {

    public static ThemeResult from(Theme theme) {
        return new ThemeResult(theme.getId(), theme.getName(), theme.getContent(), theme.getUrl());
    }
}
