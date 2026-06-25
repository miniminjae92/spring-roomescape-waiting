package roomescape.domain.reservation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationSlotRepository extends JpaRepository<ReservationSlot, Long> {

    @EntityGraph(attributePaths = {"date", "time", "theme"})
    Optional<ReservationSlot> findByDateIdAndTimeIdAndThemeId(Long dateId, Long timeId, Long themeId);

    @EntityGraph(attributePaths = {"date", "time", "theme"})
    List<ReservationSlot> findAllByThemeIdAndDateIdOrderByTimeStartAt(Long themeId, Long dateId);

    boolean existsByDateIdAndTimeIdAndThemeId(Long dateId, Long timeId, Long themeId);
}
