package roomescape.domain.reservation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservationSlotCreationRequest(
    @NotNull(message = "예약 날짜는 필수입니다")
    Long dateId,

    @NotNull(message = "예약 시간은 필수입니다")
    Long timeId,

    @NotNull(message = "테마는 필수입니다")
    Long themeId,

    @Positive(message = "회차 가격은 0원보다 커야 합니다")
    Long price
) {
}
