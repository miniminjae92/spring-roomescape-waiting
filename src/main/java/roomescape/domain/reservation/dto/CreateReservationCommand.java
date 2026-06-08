package roomescape.domain.reservation.dto;

public record CreateReservationCommand(
    String name,
    Long dateId,
    Long timeId,
    Long themeId
) {
}
