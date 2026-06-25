package roomescape.domain.reservation.dto;

import jakarta.validation.constraints.NotNull;

public record AdminReservationCreationRequest(
    @NotNull(message = "예약 회원 선택은 필수입니다")
    Long memberId,

    @NotNull(message = "예약 날짜 선택은 필수입니다")
    Long dateId,

    @NotNull(message = "예약 시간 선택은 필수입니다")
    Long timeId,

    @NotNull(message = "테마 선택은 필수입니다")
    Long themeId
) {

    public ReservationCreationRequest toReservationCreationRequest() {
        return new ReservationCreationRequest(dateId, timeId, themeId);
    }
}
