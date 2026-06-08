package roomescape.domain.theme.dto;

public record CreateThemeCommand(
    String name,
    String content,
    String url
) {
}
