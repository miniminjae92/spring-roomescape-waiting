package roomescape.domain.waitingreservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRank;

public interface WaitingReservationRepository {

    WaitingReservation save(WaitingReservation waitingReservation);

    boolean existsByNameAndDateIdAndTimeIdAndThemeId(String name, long dateId, long timeId, long themeId);

    Optional<WaitingReservation> findOldestBySlot(long dateId, long timeId, long themeId);

    default Optional<WaitingReservation> findOldestBySlotForUpdate(long dateId, long timeId, long themeId) {
        return findOldestBySlot(dateId, timeId, themeId);
    }

    List<WaitingReservationWithRank> findAllByNameWithRank(String name);

    List<WaitingReservationWithRank> findUpcomingByNameWithRank(
        String name,
        LocalDate currentDate,
        LocalTime currentTime
    );

    int deleteById(Long id);

    Optional<WaitingReservation> findById(Long id);

    default Optional<WaitingReservation> findByIdForUpdate(Long id) {
        return findById(id);
    }

    default int cancelById(Long id) {
        return deleteById(id);
    }

    default int promote(Long id, Long reservationId) {
        return deleteById(id);
    }

    default boolean existsActiveBySlot(long dateId, long timeId, long themeId) {
        return findOldestBySlot(dateId, timeId, themeId).isPresent();
    }

    default List<WaitingReservation> findAll() {
        return List.of();
    }
}
