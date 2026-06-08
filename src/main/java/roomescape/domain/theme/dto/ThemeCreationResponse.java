package roomescape.domain.theme.dto;

public record ThemeCreationResponse(
    Long id,
    String name,
    String content,
    String url
) {

    public static ThemeCreationResponse from(ThemeResult theme) {
        return new ThemeCreationResponse(
            theme.id(),
            theme.name(),
            theme.content(),
            theme.url()
        );
    }
}
