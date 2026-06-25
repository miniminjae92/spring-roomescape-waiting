package roomescape.domain.reservation;

import jakarta.validation.Valid;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.dto.ReservationCreationRequest;
import roomescape.domain.reservation.dto.ReservationCreationResponse;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.reservation.dto.ReservationUpdateRequest;
import roomescape.domain.waitingreservation.WaitingReservation;
import roomescape.domain.waitingreservation.WaitingReservationRepository;
import roomescape.support.exception.ReservationDateErrorCode;
import roomescape.support.exception.ReservationErrorCode;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.MemberErrorCode;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationSlotResolver reservationSlotResolver;
    private final WaitingReservationRepository waitingReservationRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    @Transactional
    public ReservationCreationResponse createReservation(Long memberId, ReservationCreationRequest request) {
        Member member = getMember(memberId);
        ReservationSlot slot = reservationSlotResolver.resolve(request.dateId(), request.timeId(), request.themeId());
        validateReservableDate(slot);
        validateNotDuplicated(slot);
        Reservation reservation = Reservation.createWithoutId(
                member.getName(),
                member,
                slot,
                LocalDateTime.now(clock)
        );
        Reservation savedReservation = saveReservation(reservation);
        return ReservationCreationResponse.from(savedReservation);
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> getReservationsByMember(Long memberId) {
        return reservationRepository.findUpcomingByMemberId(memberId, LocalDate.now(clock), LocalTime.now(clock)).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = getReservation(id);
        cancel(reservation);
    }

    @Transactional
    public void cancelReservation(Long memberId, Long id) {
        Reservation reservation = getReservation(id);
        validateOwner(reservation, memberId);
        validateReservableDate(reservation);

        cancel(reservation);
        reservationRepository.flush();
        promoteOldestWaiting(ReservationSlot.from(reservation));
    }

    @Transactional
    public ReservationResponse updateReservation(Long memberId, Long id, @Valid ReservationUpdateRequest request) {
        Reservation reservation = getReservation(id);
        validateOwner(reservation, memberId);
        validateReservableDate(reservation);

        ReservationSlot currentSlot = ReservationSlot.from(reservation);
        ReservationSlot newSlot = reservationSlotResolver.resolveWithTheme(
                request.dateId(),
                request.timeId(),
                reservation.getTheme()
        );

        boolean sameSlot = currentSlot.isSameSlot(newSlot);
        if (sameSlot) {
            throw new RoomescapeException(ReservationErrorCode.RESERVATION_NOT_CHANGED);
        }

        validateReservableDate(newSlot);
        validateNotDuplicated(newSlot);
        reservation.changeSlot(newSlot);
        reservationRepository.flush();
        promoteOldestWaiting(currentSlot);
        return ReservationResponse.from(reservation);
    }

    private void promoteOldestWaiting(ReservationSlot slot) {
        Optional<WaitingReservation> waitingReservationOpt = waitingReservationRepository.findOldestBySlot(
                slot.getId()
        );
        if (waitingReservationOpt.isEmpty()) {
            return;
        }

        WaitingReservation waitingReservation = waitingReservationOpt.get();
        reservationRepository.save(Reservation.createWithoutId(
                waitingReservation.getName(),
                waitingReservation.getMember(),
                waitingReservation.getSlot(),
                LocalDateTime.now(clock)
        ));
        waitingReservation.convert();
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(ReservationErrorCode.RESERVATION_NOT_FOUND));
    }

    private void validateNotDuplicated(ReservationSlot slot) {
        if (reservationRepository.existsBySlotIdAndActiveSlotTrue(slot.getId())) {
            throw new RoomescapeException(ReservationErrorCode.RESERVATION_DUPLICATED);
        }
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new RoomescapeException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateOwner(Reservation reservation, Long memberId) {
        if (!reservation.isOwnedBy(memberId)) {
            throw new RoomescapeException(ReservationErrorCode.RESERVATION_ACCESS_DENIED);
        }
    }

    private void cancel(Reservation reservation) {
        reservation.cancel(LocalDateTime.now(clock));
    }

    private Reservation saveReservation(Reservation reservation) {
        try {
            return reservationRepository.saveAndFlush(reservation);
        } catch (DataIntegrityViolationException exception) {
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
