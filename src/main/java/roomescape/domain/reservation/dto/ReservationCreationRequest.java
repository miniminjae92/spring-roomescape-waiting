package roomescape.domain.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReservationCreationRequest(
    @NotBlank(message = "예약자명은 필수입니다")
    String name,

    @NotNull(message = "예약 날짜 선택은 필수입니다")
    Long dateId,

    @NotNull(message = "예약 시간 선택은 필수입니다")
    Long timeId,

    @NotNull(message = "테마 선택은 필수입니다")
    Long themeId
) {

    public CreateReservationCommand toCommand() {
        return new CreateReservationCommand(name, dateId, timeId, themeId);
    }
}
