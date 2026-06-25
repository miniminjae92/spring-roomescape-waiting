package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    int countBySlotTimeId(Long timeId);

    int countBySlotDateId(Long dateId);

    List<Reservation> findBySlotThemeIdAndSlotDateId(Long themeId, Long dateId);

    default List<Long> findReservedTimes(Long themeId, Long dateId) {
        return findBySlotThemeIdAndSlotDateId(themeId, dateId).stream()
                .map(reservation -> reservation.getTime().getId())
                .toList();
    }

    int countBySlotThemeId(Long id);

    List<Reservation> findByName(String name);

    @EntityGraph(attributePaths = {"slot", "slot.date", "slot.time", "slot.theme"})
    @Query("""
            select r
            from Reservation r
            where r.name = :name
              and r.activeSlot = true
              and (r.slot.date.playDay > :currentDate
                or (r.slot.date.playDay = :currentDate and r.slot.time.startAt > :currentTime))
            order by r.slot.date.playDay, r.slot.time.startAt
            """)
    List<Reservation> findUpcomingByName(
            @Param("name") String name,
            @Param("currentDate") LocalDate currentDate,
            @Param("currentTime") LocalTime currentTime
    );

    @EntityGraph(attributePaths = {"slot", "slot.date", "slot.time", "slot.theme", "member"})
    @Query("""
            select r
            from Reservation r
            where r.member.id = :memberId
              and r.activeSlot = true
              and (r.slot.date.playDay > :currentDate
                or (r.slot.date.playDay = :currentDate and r.slot.time.startAt > :currentTime))
            order by r.slot.date.playDay, r.slot.time.startAt
            """)
    List<Reservation> findUpcomingByMemberId(
        @Param("memberId") Long memberId,
        @Param("currentDate") LocalDate currentDate,
        @Param("currentTime") LocalTime currentTime
    );

    boolean existsBySlotIdAndActiveSlotTrue(Long slotId);

    @EntityGraph(attributePaths = {"slot", "slot.date", "slot.time", "slot.theme", "member"})
    List<Reservation> findAllByActiveSlotTrue();
}
