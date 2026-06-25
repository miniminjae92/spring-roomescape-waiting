package roomescape.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationSlotStatus;

public record ReservationSlotResponse(
    Long id,
    Long themeId,
    LocalDate date,
    Long timeId,
    @JsonFormat(pattern = "HH:mm")
    LocalTime startAt,
    long price,
    ReservationSlotStatus status,
    boolean available
) {

    public static ReservationSlotResponse from(ReservationSlot slot, boolean available) {
        return new ReservationSlotResponse(
            slot.getId(),
            slot.themeId(),
            slot.getDate().getPlayDay(),
            slot.timeId(),
            slot.getTime().getStartAt(),
            slot.getPrice(),
            slot.getStatus(),
            available
        );
    }
}
