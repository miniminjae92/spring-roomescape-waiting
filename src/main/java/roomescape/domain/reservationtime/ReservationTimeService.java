package roomescape.domain.reservationtime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationtime.dto.CreateReservationTimeCommand;
import roomescape.domain.reservationtime.dto.ReservationTimeAvailabilityResult;
import roomescape.domain.reservationtime.dto.ReservationTimeResult;
import roomescape.support.exception.ReservationTimeErrorCode;
import roomescape.support.exception.RoomescapeException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    private static boolean isAvailable(ReservationTime reservationTime, Set<Long> reservedTimeIds) {
        return !reservedTimeIds.contains(reservationTime.getId());
    }

    public ReservationTimeResult createReservationTime(CreateReservationTimeCommand command) {
        if (reservationTimeRepository.existsByStartAt(command.startAt())) {
            throw new RoomescapeException(ReservationTimeErrorCode.RESERVATION_TIME_DUPLICATED);
        }
        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(command.startAt())
        );
        return ReservationTimeResult.from(reservationTime);
    }

    public List<ReservationTimeResult> getAllReservationTime() {
        return reservationTimeRepository.findAll().stream()
            .map(ReservationTimeResult::from)
            .toList();
    }

    public void deleteReservationTime(Long id) {
        if (reservationRepository.countByTimeId(id) > 0) {
            throw new RoomescapeException(ReservationTimeErrorCode.RESERVATION_TIME_IN_USE);
        }
        int deletedCount = reservationTimeRepository.deleteById(id);
        if (deletedCount == 0) {
            log.warn("이미 삭제된 예약 시간 삭제 요청이 들어왔습니다. timeId={}", id);
        }
    }

    public List<ReservationTimeAvailabilityResult> getReservationTimeAvailability(Long themeId, Long dateId) {
        List<ReservationTime> allReservationTime = reservationTimeRepository.findAll();
        Set<Long> reservedTimeIds = getReservedTimeIds(themeId, dateId);
        return allReservationTime.stream()
            .map(reservationTime -> ReservationTimeAvailabilityResult.of(
                reservationTime,
                isAvailable(reservationTime, reservedTimeIds)
            ))
            .toList();
    }

    public ReservationTime findById(Long id) {
        return reservationTimeRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException(ReservationTimeErrorCode.RESERVATION_TIME_NOT_EXIST));
    }

    private Set<Long> getReservedTimeIds(Long themeId, Long dateId) {
        List<Long> reservedTimeIds = reservationRepository.findReservedTimes(themeId, dateId);
        return new HashSet<>(reservedTimeIds);
    }
}
