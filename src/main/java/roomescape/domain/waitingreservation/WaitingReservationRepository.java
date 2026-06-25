package roomescape.domain.waitingreservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRank;

public interface WaitingReservationRepository extends JpaRepository<WaitingReservation, Long> {

    boolean existsByMemberIdAndDateIdAndTimeIdAndThemeIdAndStatus(
        Long memberId,
        Long dateId,
        Long timeId,
        Long themeId,
        WaitingReservationStatus status
    );

    @EntityGraph(attributePaths = {"date", "time", "theme"})
    Optional<WaitingReservation> findFirstByDateIdAndTimeIdAndThemeIdOrderByCreatedAtAscIdAsc(
            Long dateId,
            Long timeId,
            Long themeId
    );

    Optional<WaitingReservation> findFirstByDateIdAndTimeIdAndThemeIdAndStatusOrderByCreatedAtAscIdAsc(
        Long dateId,
        Long timeId,
        Long themeId,
        WaitingReservationStatus status
    );

    default Optional<WaitingReservation> findOldestBySlot(long dateId, long timeId, long themeId) {
        return findFirstByDateIdAndTimeIdAndThemeIdAndStatusOrderByCreatedAtAscIdAsc(
            dateId,
            timeId,
            themeId,
            WaitingReservationStatus.WAITING
        );
    }

    @EntityGraph(attributePaths = {"date", "time", "theme"})
    @Query("""
            select new roomescape.domain.waitingreservation.dto.WaitingReservationWithRank(
                w,
                (
                    select count(w2) + 1
                    from WaitingReservation w2
                    where w2.date = w.date
                      and w2.time = w.time
                      and w2.theme = w.theme
                      and (
                        w2.createdAt < w.createdAt
                        or (w2.createdAt = w.createdAt and w2.id < w.id)
                      )
                )
            )
            from WaitingReservation w
            where w.name = :name
            order by w.date.playDay, w.time.startAt, w.id
            """)
    List<WaitingReservationWithRank> findAllByNameWithRank(@Param("name") String name);

    @EntityGraph(attributePaths = {"date", "time", "theme"})
    @Query("""
            select new roomescape.domain.waitingreservation.dto.WaitingReservationWithRank(
                w,
                (
                    select count(w2) + 1
                    from WaitingReservation w2
                    where w2.date = w.date
                      and w2.time = w.time
                      and w2.theme = w.theme
                      and (
                        w2.createdAt < w.createdAt
                        or (w2.createdAt = w.createdAt and w2.id < w.id)
                      )
                )
            )
            from WaitingReservation w
            where w.name = :name
              and (w.date.playDay > :currentDate
                or (w.date.playDay = :currentDate and w.time.startAt > :currentTime))
            order by w.date.playDay, w.time.startAt, w.id
            """)
    List<WaitingReservationWithRank> findUpcomingByNameWithRank(
        @Param("name")
        String name,
        @Param("currentDate")
        LocalDate currentDate,
        @Param("currentTime")
        LocalTime currentTime
    );

    @EntityGraph(attributePaths = {"date", "time", "theme", "member"})
    @Query("""
            select new roomescape.domain.waitingreservation.dto.WaitingReservationWithRank(
                w,
                (
                    select count(w2) + 1
                    from WaitingReservation w2
                    where w2.date = w.date
                      and w2.time = w.time
                      and w2.theme = w.theme
                      and w2.status = roomescape.domain.waitingreservation.WaitingReservationStatus.WAITING
                      and (
                        w2.createdAt < w.createdAt
                        or (w2.createdAt = w.createdAt and w2.id < w.id)
                      )
                )
            )
            from WaitingReservation w
            where w.member.id = :memberId
              and w.status = roomescape.domain.waitingreservation.WaitingReservationStatus.WAITING
              and (w.date.playDay > :currentDate
                or (w.date.playDay = :currentDate and w.time.startAt > :currentTime))
            order by w.date.playDay, w.time.startAt, w.id
            """)
    List<WaitingReservationWithRank> findUpcomingByMemberIdWithRank(
        @Param("memberId") Long memberId,
        @Param("currentDate") LocalDate currentDate,
        @Param("currentTime") LocalTime currentTime
    );

}
