package roomescape.domain.reservation;

import java.time.Clock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.dto.ReservationSlotCreationRequest;
import roomescape.domain.reservation.dto.ReservationSlotResponse;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.support.exception.ReservationDateErrorCode;
import roomescape.support.exception.ReservationErrorCode;
import roomescape.support.exception.ReservationTimeErrorCode;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.ThemeErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationSlotService {

    private final ReservationSlotRepository slotRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationDateRepository dateRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    @Transactional
    public ReservationSlotResponse create(ReservationSlotCreationRequest request) {
        ReservationDate date = dateRepository.findById(request.dateId())
            .orElseThrow(() -> new RoomescapeException(ReservationDateErrorCode.RESERVATION_DATE_NOT_EXIST));
        ReservationTime time = timeRepository.findById(request.timeId())
            .orElseThrow(() -> new RoomescapeException(ReservationTimeErrorCode.RESERVATION_TIME_NOT_EXIST));
        Theme theme = themeRepository.findById(request.themeId())
            .orElseThrow(() -> new RoomescapeException(ThemeErrorCode.THEME_NOT_EXIST));
        long price = request.price() == null ? theme.getPrice() : request.price();

        try {
            ReservationSlot slot = slotRepository.saveAndFlush(
                ReservationSlot.createWithoutId(date, time, theme, price)
            );
            return ReservationSlotResponse.from(slot, true);
        } catch (DataIntegrityViolationException exception) {
            throw new RoomescapeException(ReservationErrorCode.RESERVATION_SLOT_DUPLICATED);
        }
    }

    public List<ReservationSlotResponse> findByThemeAndDate(Long themeId, Long dateId) {
        return slotRepository.findAllByThemeIdAndDateIdOrderByTimeStartAt(themeId, dateId).stream()
            .map(slot -> ReservationSlotResponse.from(
                slot,
                slot.getStatus() == ReservationSlotStatus.OPEN
                    && !slot.isClosedForReservation(clock)
                    && !reservationRepository.existsBySlotIdAndActiveSlotTrue(slot.getId())
            ))
            .toList();
    }

    @Transactional
    public void close(Long slotId) {
        getSlot(slotId).close();
    }

    @Transactional
    public void open(Long slotId) {
        getSlot(slotId).open();
    }

    private ReservationSlot getSlot(Long slotId) {
        return slotRepository.findById(slotId)
            .orElseThrow(() -> new RoomescapeException(ReservationErrorCode.RESERVATION_SLOT_NOT_FOUND));
    }
}
