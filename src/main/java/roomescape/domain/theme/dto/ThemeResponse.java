package roomescape.domain.theme.dto;

public record ThemeResponse(
    Long id,
    String name,
    String content,
    String url
) {

    public static ThemeResponse from(ThemeResult theme) {
        return new ThemeResponse(
            theme.id(),
            theme.name(),
            theme.content(),
            theme.url()
        );
    }
}
