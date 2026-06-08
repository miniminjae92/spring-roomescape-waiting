package roomescape.domain.reservation.dto;

public record ChangeReservationCommand(
    Long reservationId,
    Long dateId,
    Long timeId
) {
}
