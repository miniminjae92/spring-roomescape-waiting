package roomescape.domain.waitingreservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.waitingreservation.WaitingReservation;
import roomescape.domain.waitingreservation.WaitingReservationStatus;

public record WaitingReservationWithRankResponse(
    Long id,
    String name,
    LocalDate date,
    ReservationResponse.ReservationTimePayload time,
    ReservationResponse.ThemePayload theme,
    Long rank,
    LocalDateTime createdAt,
    WaitingReservationStatus status
) {

    public WaitingReservationWithRankResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationResponse.ReservationTimePayload time,
        ReservationResponse.ThemePayload theme,
        Long rank,
        LocalDateTime createdAt
    ) {
        this(id, name, date, time, theme, rank, createdAt, WaitingReservationStatus.WAITING);
    }

    public static WaitingReservationWithRankResponse from(WaitingReservationWithRank waitingReservationWithRank) {
        WaitingReservation waitingReservation = waitingReservationWithRank.waitingReservation();
        return from(WaitingReservationResult.from(waitingReservation, waitingReservationWithRank.rank()));
    }

    public static WaitingReservationWithRankResponse from(WaitingReservationResult result) {
        return new WaitingReservationWithRankResponse(
            result.id(),
            result.name(),
            result.date(),
            ReservationResponse.ReservationTimePayload.from(result.time()),
            ReservationResponse.ThemePayload.from(result.theme()),
            result.rank(),
            result.createdAt(),
            result.status()
        );
    }
}
