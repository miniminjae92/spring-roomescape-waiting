package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    int deleteById(Long id);

    int countByTimeId(Long timeId);

    int countByReservationDateId(Long dateId);

    List<Long> findReservedTimes(Long themeId, Long dateId);

    int countByThemeId(Long id);

    List<Reservation> findByName(String name);

    List<Reservation> findUpcomingByName(String name, LocalDate currentDate, LocalTime currentTime);

    Optional<Reservation> findById(Long id);

    default Optional<Reservation> findByIdForUpdate(Long id) {
        return findById(id);
    }

    default Optional<Reservation> findActiveBySlotForUpdate(Long dateId, Long timeId, Long themeId) {
        return Optional.empty();
    }

    int updateReservation(Long id, Long dateId, Long timeId);

    boolean existsByDateIdAndTimeIdAndThemeId(Long dateId, Long timeId, Long themeId);

    default int cancelById(Long id) {
        return deleteById(id);
    }
}
