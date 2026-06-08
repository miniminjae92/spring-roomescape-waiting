package roomescape.domain.theme.dto;

public record AdminThemeResponse(
    Long id,
    String name,
    String content,
    String url
) {

    public static AdminThemeResponse from(ThemeResult theme) {
        return new AdminThemeResponse(
            theme.id(),
            theme.name(),
            theme.content(),
            theme.url()
        );
    }
}
