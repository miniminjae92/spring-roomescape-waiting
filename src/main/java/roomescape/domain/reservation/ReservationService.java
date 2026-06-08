package roomescape.domain.reservation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.dto.ChangeReservationCommand;
import roomescape.domain.reservation.dto.CreateReservationCommand;
import roomescape.domain.reservation.dto.ReservationResult;
import roomescape.domain.waitingreservation.WaitingReservation;
import roomescape.domain.waitingreservation.WaitingReservationRepository;
import roomescape.support.exception.ReservationDateErrorCode;
import roomescape.support.exception.ReservationErrorCode;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.RoomescapeErrorCode;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationSlotResolver reservationSlotResolver;
    private final WaitingReservationRepository waitingReservationRepository;
    private final Clock clock;

    public ReservationResult createReservation(CreateReservationCommand command) {
        ReservationSlot slot = reservationSlotResolver.resolve(command.dateId(), command.timeId(), command.themeId());
        validateReservableDate(slot);
        validateNotDuplicated(slot);
        Reservation savedReservation;
        try {
            savedReservation = reservationRepository.save(
                Reservation.createWithoutId(command.name(), slot.date(), slot.time(), slot.theme()));
        } catch (DuplicateKeyException e) {
            throw new RoomescapeException(ReservationErrorCode.RESERVATION_DUPLICATED);
        }
        return ReservationResult.from(savedReservation);
    }

    public List<ReservationResult> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResult::from)
                .toList();
    }

    public List<ReservationResult> getReservationsByName(String name) {
        return reservationRepository.findUpcomingByName(name, LocalDate.now(clock), LocalTime.now(clock)).stream()
                .map(ReservationResult::from)
                .toList();
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = getReservationForUpdate(id);
        ReservationSlot slot = ReservationSlot.from(reservation);
        if (waitingReservationRepository.existsActiveBySlot(slot.dateId(), slot.timeId(), slot.themeId())) {
            throw new RoomescapeException(ReservationErrorCode.RESERVATION_HAS_ACTIVE_WAITING);
        }
        int deletedCount = reservationRepository.deleteById(id);
        if (deletedCount == 0) {
            throw new RoomescapeException(ReservationErrorCode.RESERVATION_NOT_FOUND);
        }
    }

    @Transactional
    public void cancelReservation(Long id) {
        Reservation reservation = getReservationForUpdate(id);
        validateReservableDate(reservation);
        cancelReservationOrThrow(id);
        promoteOldestWaiting(ReservationSlot.from(reservation));
    }

    @Transactional
    public void cancelReservationByAdmin(Long id) {
        Reservation reservation = getReservationForUpdate(id);
        cancelReservationOrThrow(id);
        if (!ReservationSlot.from(reservation).hasStarted(clock)) {
            promoteOldestWaiting(ReservationSlot.from(reservation));
        }
    }

    @Transactional
    public ReservationResult updateReservation(ChangeReservationCommand command) {
        Reservation reservation = getReservationForUpdate(command.reservationId());
        validateReservableDate(reservation);

        ReservationSlot currentSlot = ReservationSlot.from(reservation);
        ReservationSlot newSlot = reservationSlotResolver.resolveWithTheme(
                command.dateId(),
                command.timeId(),
                reservation.getTheme()
        );

        boolean sameSlot = currentSlot.isSameSlot(newSlot);
        if (sameSlot) {
            throw new RoomescapeException(ReservationErrorCode.RESERVATION_NOT_CHANGED);
        }

        validateReservableDate(newSlot);
        validateNotDuplicated(newSlot);
        updateReservationOrThrow(command);
        promoteOldestWaiting(currentSlot);
        return ReservationResult.from(getReservation(command.reservationId()));
    }

    private void promoteOldestWaiting(ReservationSlot slot) {
        Optional<WaitingReservation> waitingReservationOpt = waitingReservationRepository.findOldestBySlotForUpdate(
                slot.dateId(),
                slot.timeId(),
                slot.themeId()
        );
        if (waitingReservationOpt.isEmpty()) {
            return;
        }

        WaitingReservation waitingReservation = waitingReservationOpt.get();
        Reservation promotedReservation = reservationRepository.save(Reservation.createWithoutId(
                waitingReservation.getName(),
                waitingReservation.getDate(),
                waitingReservation.getTime(),
                waitingReservation.getTheme()
        ));
        promoteWaitingReservationOrThrow(waitingReservation.getId(), promotedReservation.getId());
    }

    private void cancelReservationOrThrow(Long id) {
        int updatedCount = reservationRepository.cancelById(id);
        if (updatedCount == 0) {
            throw new RoomescapeException(RoomescapeErrorCode.DATA_CONSISTENCY_VIOLATION);
        }
    }

    private void promoteWaitingReservationOrThrow(Long id, Long reservationId) {
        int updatedCount = waitingReservationRepository.promote(id, reservationId);
        if (updatedCount == 0) {
            throw new RoomescapeException(RoomescapeErrorCode.DATA_CONSISTENCY_VIOLATION);
        }
    }

    private void updateReservationOrThrow(ChangeReservationCommand command) {
        int updatedCount = reservationRepository.updateReservation(
            command.reservationId(),
            command.dateId(),
            command.timeId()
        );
        if (updatedCount == 0) {
            throw new RoomescapeException(RoomescapeErrorCode.DATA_CONSISTENCY_VIOLATION);
        }
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(ReservationErrorCode.RESERVATION_NOT_FOUND));
    }

    private Reservation getReservationForUpdate(Long id) {
        return reservationRepository.findByIdForUpdate(id)
            .orElseThrow(() -> new RoomescapeException(ReservationErrorCode.RESERVATION_NOT_FOUND));
    }

    private void validateNotDuplicated(ReservationSlot slot) {
        if (reservationRepository.existsByDateIdAndTimeIdAndThemeId(slot.dateId(), slot.timeId(), slot.themeId())) {
            throw new RoomescapeException(ReservationErrorCode.RESERVATION_DUPLICATED);
        }
    }

    private void validateReservableDate(Reservation reservation) {
        ReservationSlot slot = ReservationSlot.from(reservation);
        if (slot.isClosedForReservation(clock)) {
            throw new RoomescapeException(ReservationDateErrorCode.RESERVATION_DATE_NOT_ALLOWED);
        }
    }

    private void validateReservableDate(ReservationSlot slot) {
        if (slot.isClosedForReservation(clock)) {
            throw new RoomescapeException(ReservationDateErrorCode.RESERVATION_DATE_NOT_ALLOWED);
        }
    }
}
