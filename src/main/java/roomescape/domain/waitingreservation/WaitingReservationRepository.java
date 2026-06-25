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

    boolean existsByMemberIdAndSlotIdAndStatus(
        Long memberId,
        Long slotId,
        WaitingReservationStatus status
    );

    @EntityGraph(attributePaths = {"slot", "slot.date", "slot.time", "slot.theme"})
    Optional<WaitingReservation> findFirstBySlotIdAndStatusOrderByCreatedAtAscIdAsc(
        Long slotId,
        WaitingReservationStatus status
    );

    default Optional<WaitingReservation> findOldestBySlot(long slotId) {
        return findFirstBySlotIdAndStatusOrderByCreatedAtAscIdAsc(
            slotId,
            WaitingReservationStatus.WAITING
        );
    }

    @EntityGraph(attributePaths = {"slot", "slot.date", "slot.time", "slot.theme", "member"})
    @Query("""
            select new roomescape.domain.waitingreservation.dto.WaitingReservationWithRank(
                w,
                (
                    select count(w2) + 1
                    from WaitingReservation w2
                    where w2.slot = w.slot
                      and w2.status = roomescape.domain.waitingreservation.WaitingReservationStatus.WAITING
                      and (
                        w2.createdAt < w.createdAt
                        or (w2.createdAt = w.createdAt and w2.id < w.id)
                      )
                )
            )
            from WaitingReservation w
            where w.status = roomescape.domain.waitingreservation.WaitingReservationStatus.WAITING
            order by w.slot.date.playDay, w.slot.time.startAt, w.createdAt, w.id
            """)
    List<WaitingReservationWithRank> findAllWaitingWithRank();

    @EntityGraph(attributePaths = {"slot", "slot.date", "slot.time", "slot.theme"})
    @Query("""
            select new roomescape.domain.waitingreservation.dto.WaitingReservationWithRank(
                w,
                (
                    select count(w2) + 1
                    from WaitingReservation w2
                    where w2.slot = w.slot
                      and (
                        w2.createdAt < w.createdAt
                        or (w2.createdAt = w.createdAt and w2.id < w.id)
                      )
                )
            )
            from WaitingReservation w
            where w.name = :name
            order by w.slot.date.playDay, w.slot.time.startAt, w.id
            """)
    List<WaitingReservationWithRank> findAllByNameWithRank(@Param("name") String name);

    @EntityGraph(attributePaths = {"slot", "slot.date", "slot.time", "slot.theme"})
    @Query("""
            select new roomescape.domain.waitingreservation.dto.WaitingReservationWithRank(
                w,
                (
                    select count(w2) + 1
                    from WaitingReservation w2
                    where w2.slot = w.slot
                      and (
                        w2.createdAt < w.createdAt
                        or (w2.createdAt = w.createdAt and w2.id < w.id)
                      )
                )
            )
            from WaitingReservation w
            where w.name = :name
              and (w.slot.date.playDay > :currentDate
                or (w.slot.date.playDay = :currentDate and w.slot.time.startAt > :currentTime))
            order by w.slot.date.playDay, w.slot.time.startAt, w.id
            """)
    List<WaitingReservationWithRank> findUpcomingByNameWithRank(
        @Param("name")
        String name,
        @Param("currentDate")
        LocalDate currentDate,
        @Param("currentTime")
        LocalTime currentTime
    );

    @EntityGraph(attributePaths = {"slot", "slot.date", "slot.time", "slot.theme", "member"})
    @Query("""
            select new roomescape.domain.waitingreservation.dto.WaitingReservationWithRank(
                w,
                (
                    select count(w2) + 1
                    from WaitingReservation w2
                    where w2.slot = w.slot
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
              and (w.slot.date.playDay > :currentDate
                or (w.slot.date.playDay = :currentDate and w.slot.time.startAt > :currentTime))
            order by w.slot.date.playDay, w.slot.time.startAt, w.id
            """)
    List<WaitingReservationWithRank> findUpcomingByMemberIdWithRank(
        @Param("memberId") Long memberId,
        @Param("currentDate") LocalDate currentDate,
        @Param("currentTime") LocalTime currentTime
    );

}
