package roomescape.domain.reservationtime.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record TimeCreationRequest(
    @JsonFormat(pattern = "HH:mm")
    @NotNull(message = "시작 시간은 필수입니다")
    LocalTime startAt
) {

    public CreateReservationTimeCommand toCommand() {
        return new CreateReservationTimeCommand(startAt);
    }
}
