package roomescape.domain.theme.dto;

public record ThemeRankResponse(
    Long id,
    String name,
    String url
) {

    public static ThemeRankResponse from(ThemeResult theme) {
        return new ThemeRankResponse(
            theme.id(),
            theme.name(),
            theme.url()
        );
    }
}
