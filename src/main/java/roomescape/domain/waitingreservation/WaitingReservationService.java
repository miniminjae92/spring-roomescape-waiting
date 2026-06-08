package roomescape.domain.waitingreservation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationSlotResolver;
import roomescape.domain.waitingreservation.dto.CreateWaitingReservationCommand;
import roomescape.domain.waitingreservation.dto.WaitingReservationResult;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.WaitingReservationErrorCode;

@Service
@RequiredArgsConstructor
public class WaitingReservationService {

    private final WaitingReservationRepository waitingReservationRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSlotResolver reservationSlotResolver;
    private final Clock clock;

    @Transactional
    public WaitingReservationResult createWaitingReservation(CreateWaitingReservationCommand command) {
        ReservationSlot slot = reservationSlotResolver.resolve(command.dateId(), command.timeId(), command.themeId());
        validateReservableDate(slot);
        lockReservedSlot(slot);
        validateDuplicationOfWaitingReservation(command.name(), slot);

        WaitingReservation waitingReservation = WaitingReservation.createWithoutId(
                command.name(),
                slot.date(),
                slot.time(),
                slot.theme(),
                LocalDateTime.now(clock)
        );
        WaitingReservation savedWaitingReservation;
        try {
            savedWaitingReservation = waitingReservationRepository.save(waitingReservation);
        } catch (DuplicateKeyException e) {
            throw new RoomescapeException(WaitingReservationErrorCode.DUPLICATE_WAITING_RESERVATION);
        }
        return WaitingReservationResult.from(savedWaitingReservation);
    }

    private void validateDuplicationOfWaitingReservation(String name, ReservationSlot slot) {
        if (waitingReservationRepository.existsByNameAndDateIdAndTimeIdAndThemeId(
            name,
            slot.dateId(),
            slot.timeId(),
            slot.themeId()
        )) {
            throw new RoomescapeException(WaitingReservationErrorCode.DUPLICATE_WAITING_RESERVATION);
        }
    }

    private void validateSlotIsReserved(ReservationSlot slot) {
        boolean reserved = reservationRepository.existsByDateIdAndTimeIdAndThemeId(
            slot.dateId(),
            slot.timeId(),
            slot.themeId()
        );
        if (!reserved) {
            throw new RoomescapeException(WaitingReservationErrorCode.AVAILABLE_SLOT_NOT_WAITABLE);
        }
    }

    private void lockReservedSlot(ReservationSlot slot) {
        boolean locked = reservationRepository.findActiveBySlotForUpdate(
            slot.dateId(),
            slot.timeId(),
            slot.themeId()
        ).isPresent();
        if (!locked) {
            validateSlotIsReserved(slot);
        }
    }

    private void validateReservableDate(ReservationSlot slot) {
        if (slot.isClosedForReservation(clock)) {
            throw new RoomescapeException(WaitingReservationErrorCode.WAITING_RESERVATION_DATE_NOT_ALLOWED);
        }
    }

    public void cancelWaitingReservation(Long id) {
        int updatedCount = waitingReservationRepository.cancelById(id);
        if (updatedCount == 0) {
            throw new RoomescapeException(WaitingReservationErrorCode.WAITING_RESERVATION_NOT_FOUND);
        }
    }

    public void cancelWaitingReservationByAdmin(Long id) {
        cancelWaitingReservation(id);
    }

    public void deleteWaitingReservation(Long id) {
        int deletedCount = waitingReservationRepository.deleteById(id);
        if (deletedCount == 0) {
            throw new RoomescapeException(WaitingReservationErrorCode.WAITING_RESERVATION_NOT_FOUND);
        }
    }

    public List<WaitingReservationResult> getAllWaitingReservations() {
        return waitingReservationRepository.findAll().stream()
            .map(WaitingReservationResult::from)
            .toList();
    }

    public List<WaitingReservationResult> getWaitingReservationsWithRankByName(String name) {
        return waitingReservationRepository.findUpcomingByNameWithRank(name, LocalDate.now(clock), LocalTime.now(clock))
            .stream()
            .map(waiting -> WaitingReservationResult.from(
                waiting.waitingReservation(),
                waiting.rank()
            ))
            .toList();
    }
}
