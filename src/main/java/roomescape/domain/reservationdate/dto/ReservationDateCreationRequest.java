package roomescape.domain.reservationdate.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationDateCreationRequest(
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "플레이 날짜는 필수입니다")
    LocalDate playDay
) {

    public CreateReservationDateCommand toCommand() {
        return new CreateReservationDateCommand(playDay);
    }
}
