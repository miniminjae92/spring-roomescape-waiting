package roomescape.domain.waitingreservation.dto;

public record CreateWaitingReservationCommand(
    String name,
    Long dateId,
    Long timeId,
    Long themeId
) {
}
