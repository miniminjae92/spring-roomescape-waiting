package roomescape.domain.waitingreservation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationSlotResolver;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationRequest;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationResponse;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRankResponse;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.WaitingReservationErrorCode;
import roomescape.support.exception.MemberErrorCode;

@Service
@RequiredArgsConstructor
public class WaitingReservationService {

    private final WaitingReservationRepository waitingReservationRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSlotResolver reservationSlotResolver;
    private final MemberRepository memberRepository;
    private final Clock clock;

    @Transactional
    public WaitingReservationCreationResponse createWaitingReservation(
        Long memberId,
        WaitingReservationCreationRequest request
    ) {
        Member member = getMember(memberId);
        ReservationSlot slot = reservationSlotResolver.resolve(request.dateId(), request.timeId(), request.themeId());
        validateReservableDate(slot);
        validateSlotIsReserved(slot);
        validateDuplicationOfWaitingReservation(memberId, slot);

        WaitingReservation waitingReservation = WaitingReservation.createWithoutId(
            member.getName(),
            member,
            slot.date(),
            slot.time(),
            slot.theme(),
            LocalDateTime.now(clock)
        );
        WaitingReservation savedWaitingReservation = saveWaitingReservation(waitingReservation);
        return WaitingReservationCreationResponse.from(savedWaitingReservation);
    }

    private void validateDuplicationOfWaitingReservation(Long memberId, ReservationSlot slot) {
        if (waitingReservationRepository.existsByMemberIdAndDateIdAndTimeIdAndThemeIdAndStatus(
            memberId,
            slot.dateId(),
            slot.timeId(),
            slot.themeId(),
            WaitingReservationStatus.WAITING
        )) {
            throw new RoomescapeException(WaitingReservationErrorCode.DUPLICATE_WAITING_RESERVATION);
        }
    }

    private void validateSlotIsReserved(ReservationSlot slot) {
        boolean reserved = reservationRepository.existsByDateIdAndTimeIdAndThemeIdAndActiveSlotTrue(
            slot.dateId(),
            slot.timeId(),
            slot.themeId()
        );
        if (!reserved) {
            throw new RoomescapeException(WaitingReservationErrorCode.AVAILABLE_SLOT_NOT_WAITABLE);
        }
    }

    private void validateReservableDate(ReservationSlot slot) {
        if (slot.isClosedForReservation(clock)) {
            throw new RoomescapeException(WaitingReservationErrorCode.WAITING_RESERVATION_DATE_NOT_ALLOWED);
        }
    }

    @Transactional
    public void cancelWaitingReservation(Long memberId, Long id) {
        WaitingReservation waitingReservation = getWaitingReservation(id);
        if (!waitingReservation.isOwnedBy(memberId)) {
            throw new RoomescapeException(WaitingReservationErrorCode.WAITING_RESERVATION_ACCESS_DENIED);
        }
        waitingReservation.cancel(LocalDateTime.now(clock));
    }

    public List<WaitingReservationWithRankResponse> getWaitingReservationsWithRankByMember(Long memberId) {
        return waitingReservationRepository.findUpcomingByMemberIdWithRank(
                memberId,
                LocalDate.now(clock),
                LocalTime.now(clock)
            )
            .stream()
            .map(WaitingReservationWithRankResponse::from)
            .toList();
    }

    private WaitingReservation getWaitingReservation(Long id) {
        return waitingReservationRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException(WaitingReservationErrorCode.WAITING_RESERVATION_NOT_FOUND));
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new RoomescapeException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private WaitingReservation saveWaitingReservation(WaitingReservation waitingReservation) {
        try {
            return waitingReservationRepository.saveAndFlush(waitingReservation);
        } catch (DataIntegrityViolationException exception) {
            throw new RoomescapeException(WaitingReservationErrorCode.DUPLICATE_WAITING_RESERVATION);
        }
    }
}
