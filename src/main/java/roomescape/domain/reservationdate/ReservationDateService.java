package roomescape.domain.reservationdate;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationdate.dto.CreateReservationDateCommand;
import roomescape.domain.reservationdate.dto.ReservationDateResult;
import roomescape.support.exception.ReservationDateErrorCode;
import roomescape.support.exception.RoomescapeException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationDateService {

    private final ReservationRepository reservationRepository;
    private final ReservationDateRepository reservationDateRepository;

    public List<ReservationDateResult> getAllReservationDateForAdmin() {
        return reservationDateRepository.findAll().stream()
            .map(ReservationDateResult::from)
            .toList();
    }

    public ReservationDateResult createReservationDate(CreateReservationDateCommand command) {
        if (reservationDateRepository.existsByPlayDay(command.playDay())) {
            throw new RoomescapeException(ReservationDateErrorCode.RESERVATION_DATE_DUPLICATED);
        }
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(command.playDay())
        );
        return ReservationDateResult.from(reservationDate);
    }

    public void deleteReservationDate(Long id) {
        if (reservationRepository.countByReservationDateId(id) > 0) {
            throw new RoomescapeException(ReservationDateErrorCode.RESERVATION_DATE_IN_USE);
        }
        int deletedCount = reservationDateRepository.deleteById(id);
        if (deletedCount == 0) {
            log.warn("이미 삭제된 날짜의 삭제 요청이 들어왔습니다. dateId={}", id);
        }
    }

    public List<ReservationDateResult> getAllAvailableReservationDate() {
        return reservationDateRepository.findAll().stream()
            .filter(reservationDate -> reservationDate.isAvailable(LocalDate.now()))
            .map(ReservationDateResult::from)
            .toList();
    }

    public ReservationDate findById(Long id) {
        return reservationDateRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException(ReservationDateErrorCode.RESERVATION_DATE_NOT_EXIST));
    }
}
