package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.dto.ChangeReservationCommand;
import roomescape.domain.reservation.dto.CreateReservationCommand;
import roomescape.domain.reservation.dto.ReservationResult;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.waitingreservation.WaitingReservation;
import roomescape.domain.waitingreservation.WaitingReservationRepository;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRank;
import roomescape.support.exception.ReservationDateErrorCode;
import roomescape.support.exception.ReservationErrorCode;
import roomescape.support.exception.RoomescapeException;

class ReservationServiceTest {

    private static final Clock CLOCK = Clock.systemDefaultZone();

    private ReservationService reservationService;
    private FakeReservationRepository reservationRepository;
    private FakeReservationDateRepository reservationDateRepository;
    private FakeReservationTimeRepository reservationTimeRepository;
    private FakeThemeRepository themeRepository;
    private FakeWaitingReservationRepository waitingReservationRepository;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        reservationDateRepository = new FakeReservationDateRepository();
        reservationTimeRepository = new FakeReservationTimeRepository();
        themeRepository = new FakeThemeRepository();
        waitingReservationRepository = new FakeWaitingReservationRepository();

        reservationService = new ReservationService(
                reservationRepository,
                new ReservationSlotResolver(reservationDateRepository, reservationTimeRepository, themeRepository),
                waitingReservationRepository,
                CLOCK
        );
    }

    @Test
    @DisplayName("예약을 생성한다.")
    void createReservation() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        CreateReservationCommand request = new CreateReservationCommand("테스터", date.getId(), time.getId(),
                theme.getId());

        ReservationResult response = reservationService.createReservation(request);

        assertThat(response.name()).isEqualTo("테스터");
        assertThat(reservationRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("마감된 일시로 예약 생성 시 예외가 발생한다.")
    void createReservationWithPastTime() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().minusDays(1)));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        CreateReservationCommand request = new CreateReservationCommand("테스터", date.getId(), time.getId(),
                theme.getId());

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationDateErrorCode.RESERVATION_DATE_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("중복된 예약 생성 시 예외가 발생한다.")
    void createDuplicateReservation() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        reservationService.createReservation(
                new CreateReservationCommand("테스터1", date.getId(), time.getId(), theme.getId()));

        CreateReservationCommand duplicateRequest = new CreateReservationCommand("테스터2", date.getId(), time.getId(),
                theme.getId());

        assertThatThrownBy(() -> reservationService.createReservation(duplicateRequest))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationErrorCode.RESERVATION_DUPLICATED.getMessage());
    }

    @Test
    @DisplayName("이름으로 예약을 조회한다.")
    void getReservationsByName() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));
        reservationService.createReservation(
                new CreateReservationCommand("테스터", date.getId(), time.getId(), theme.getId()));

        List<ReservationResult> responses = reservationService.getReservationsByName("테스터");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).name()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("모든 예약을 조회한다.")
    void getAllReservations() {
        ReservationDate date1 = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        ReservationTime time1 = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        ReservationDate date2 = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(2)));
        ReservationTime time2 = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(11, 0)));

        reservationService.createReservation(
                new CreateReservationCommand("테스터1", date1.getId(), time1.getId(), theme.getId()));
        reservationService.createReservation(
                new CreateReservationCommand("테스터2", date2.getId(), time2.getId(), theme.getId()));

        List<ReservationResult> responses = reservationService.getAllReservations();

        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void cancelReservation() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        ReservationResult response = reservationService.createReservation(
                new CreateReservationCommand("테스터", date.getId(), time.getId(), theme.getId()));

        reservationService.cancelReservation(response.id());

        assertThat(reservationRepository.findById(response.id()))
                .get()
                .extracting(Reservation::getStatus)
                .isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    @DisplayName("관리자가 예약을 삭제한다.")
    void deleteReservation() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        ReservationResult response = reservationService.createReservation(
                new CreateReservationCommand("테스터", date.getId(), time.getId(), theme.getId()));

        reservationService.deleteReservation(response.id());

        assertThat(reservationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약 삭제 시 예외가 발생한다.")
    void deleteNotFoundReservation() {
        assertThatThrownBy(() -> reservationService.deleteReservation(999L))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("마감된 예약 삭제 시 예외가 발생한다.")
    void cancelClosedReservation() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now()));
        ReservationTime time = reservationTimeRepository.save(
                ReservationTime.createWithoutId(LocalTime.now().plusMinutes(9)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        Reservation reservation = reservationRepository.save(
                Reservation.createWithoutId("마감예약테스터", date, time, theme));

        assertThatThrownBy(() -> reservationService.cancelReservation(reservation.getId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationDateErrorCode.RESERVATION_DATE_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("예약을 수정한다.")
    void updateReservation() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        ReservationResult creationResponse = reservationService.createReservation(
                new CreateReservationCommand("테스터", date.getId(), time.getId(), theme.getId()));

        ReservationDate newDate = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(2)));
        ReservationTime newTime = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(14, 0)));

        ChangeReservationCommand updateCommand =
                new ChangeReservationCommand(creationResponse.id(), newDate.getId(), newTime.getId());

        ReservationResult updateResponse = reservationService.updateReservation(updateCommand);

        assertThat(updateResponse.date()).isEqualTo(newDate.getPlayDay());
        assertThat(updateResponse.time().getId()).isEqualTo(newTime.getId());
    }

    @Test
    @DisplayName("기존 예약과 같은 날짜와 시간으로 예약 수정 시 예외가 발생한다.")
    void updateReservationWithoutChange() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        ReservationResult creationResponse = reservationService.createReservation(
                new CreateReservationCommand("테스터", date.getId(), time.getId(), theme.getId()));

        ChangeReservationCommand updateCommand =
                new ChangeReservationCommand(creationResponse.id(), date.getId(), time.getId());

        assertThatThrownBy(() -> reservationService.updateReservation(updateCommand))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationErrorCode.RESERVATION_NOT_CHANGED.getMessage());
    }

    @Test
    @DisplayName("마감된 일시로 예약 수정 시 예외가 발생한다.")
    void updateReservationWithPastTime() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        ReservationResult creationResponse = reservationService.createReservation(
                new CreateReservationCommand("테스터", date.getId(), time.getId(), theme.getId()));

        ReservationDate pastDate = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().minusDays(1)));

        ChangeReservationCommand updateCommand =
                new ChangeReservationCommand(creationResponse.id(), pastDate.getId(), time.getId());

        assertThatThrownBy(() -> reservationService.updateReservation(updateCommand))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationDateErrorCode.RESERVATION_DATE_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("이미 존재하는 시간으로 예약 수정 시 예외가 발생한다.")
    void updateReservationToDuplicatedTime() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now().plusDays(1)));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        ReservationTime anotherTime = reservationTimeRepository.save(
                ReservationTime.createWithoutId(LocalTime.of(14, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        ReservationResult myReservation = reservationService.createReservation(
                new CreateReservationCommand("내예약", date.getId(), time.getId(), theme.getId()));

        reservationService.createReservation(
                new CreateReservationCommand("다른사람예약", date.getId(), anotherTime.getId(), theme.getId()));

        ChangeReservationCommand updateCommand =
                new ChangeReservationCommand(myReservation.id(), date.getId(), anotherTime.getId());

        assertThatThrownBy(() -> reservationService.updateReservation(updateCommand))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationErrorCode.RESERVATION_DUPLICATED.getMessage());
    }

    @Test
    @DisplayName("마감된 예약 수정 시 예외가 발생한다.")
    void updateClosedReservation() {
        ReservationDate date = reservationDateRepository.save(
                ReservationDate.createWithoutId(LocalDate.now()));
        ReservationTime time = reservationTimeRepository.save(
                ReservationTime.createWithoutId(LocalTime.now().plusMinutes(9)));
        ReservationTime newTime = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(14, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        Reservation reservation = reservationRepository.save(
                Reservation.createWithoutId("마감예약테스터", date, time, theme));

        ChangeReservationCommand updateCommand =
                new ChangeReservationCommand(reservation.getId(), date.getId(), newTime.getId());

        assertThatThrownBy(() -> reservationService.updateReservation(updateCommand))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationDateErrorCode.RESERVATION_DATE_NOT_ALLOWED.getMessage());
    }

    private static class FakeReservationRepository implements ReservationRepository {

        private final List<Reservation> reservations = new ArrayList<>();
        private Long idCounter = 1L;

        @Override
        public Reservation save(Reservation reservation) {
            Reservation saved = Reservation.of(idCounter++, reservation.getName(), reservation.getDate(),
                    reservation.getTime(), reservation.getTheme());
            reservations.add(saved);
            return saved;
        }

        @Override
        public List<Reservation> findAll() {
            return reservations;
        }

        @Override
        public int deleteById(Long id) {
            boolean removed = reservations.removeIf(r -> r.getId().equals(id));
            return removed ? 1 : 0;
        }

        @Override
        public int cancelById(Long id) {
            Optional<Reservation> target = findById(id);
            if (target.isEmpty()) {
                return 0;
            }
            reservations.remove(target.get());
            reservations.add(target.get().cancel());
            return 1;
        }

        @Override
        public int countByTimeId(Long timeId) {
            return (int) reservations.stream().filter(r -> r.getTime().getId().equals(timeId)).count();
        }

        @Override
        public int countByReservationDateId(Long dateId) {
            return (int) reservations.stream().filter(r -> r.getDate().getId().equals(dateId)).count();
        }

        @Override
        public List<Long> findReservedTimes(Long themeId, Long dateId) {
            return reservations.stream()
                    .filter(r -> r.getTheme().getId().equals(themeId) && r.getDate().getId().equals(dateId))
                    .map(r -> r.getTime().getId())
                    .toList();
        }

        @Override
        public int countByThemeId(Long id) {
            return (int) reservations.stream().filter(r -> r.getTheme().getId().equals(id)).count();
        }

        @Override
        public List<Reservation> findByName(String name) {
            return reservations.stream().filter(r -> r.getName().equals(name)).toList();
        }

        @Override
        public List<Reservation> findUpcomingByName(String name, LocalDate currentDate, LocalTime currentTime) {
            return findByName(name).stream()
                    .filter(r -> r.getDate().getPlayDay().isAfter(currentDate)
                            || (r.getDate().getPlayDay().isEqual(currentDate)
                            && r.getTime().getStartAt().isAfter(currentTime)))
                    .toList();
        }

        @Override
        public Optional<Reservation> findById(Long id) {
            return reservations.stream().filter(r -> r.getId().equals(id)).findFirst();
        }

        @Override
        public int updateReservation(Long id, Long dateId, Long timeId) {
            Optional<Reservation> target = findById(id);
            if (target.isPresent()) {
                Reservation existing = target.get();
                ReservationDate updatedDate = ReservationDate.of(dateId, existing.getDate().getPlayDay().plusDays(1));
                ReservationTime updatedTime = ReservationTime.of(timeId, LocalTime.now());

                Reservation updated = Reservation.of(id, existing.getName(), updatedDate, updatedTime,
                        existing.getTheme());
                reservations.remove(existing);
                reservations.add(updated);
                return 1;
            }
            return 0;
        }

        @Override
        public boolean existsByDateIdAndTimeIdAndThemeId(Long dateId, Long timeId, Long themeId) {
            return reservations.stream().anyMatch(
                    r -> r.getDate().getId().equals(dateId) && r.getTime().getId().equals(timeId) && r.getTheme()
                            .getId()
                            .equals(themeId));
        }
    }

    private static class FakeWaitingReservationRepository implements WaitingReservationRepository {

        @Override
        public WaitingReservation save(WaitingReservation waitingReservation) {
            return waitingReservation;
        }

        @Override
        public boolean existsByNameAndDateIdAndTimeIdAndThemeId(String name, long dateId, long timeId, long themeId) {
            return false;
        }

        @Override
        public Optional<WaitingReservation> findOldestBySlot(long dateId, long timeId, long themeId) {
            return Optional.empty();
        }

        @Override
        public List<WaitingReservationWithRank> findAllByNameWithRank(String name) {
            return List.of();
        }

        @Override
        public List<WaitingReservationWithRank> findUpcomingByNameWithRank(
                String name,
                LocalDate currentDate,
                LocalTime currentTime
        ) {
            return List.of();
        }

        @Override
        public int deleteById(Long id) {
            return 0;
        }

        @Override
        public Optional<WaitingReservation> findById(Long id) {
            return Optional.empty();
        }
    }

    private static class FakeReservationDateRepository implements ReservationDateRepository {

        private final List<ReservationDate> dates = new ArrayList<>();
        private Long idCounter = 1L;

        @Override
        public Optional<ReservationDate> findById(Long id) {
            return dates.stream().filter(d -> d.getId().equals(id)).findFirst();
        }

        @Override
        public List<ReservationDate> findAll() {
            return dates;
        }

        @Override
        public ReservationDate save(ReservationDate reservationDate) {
            ReservationDate saved = ReservationDate.of(idCounter++, reservationDate.getPlayDay());
            dates.add(saved);
            return saved;
        }

        @Override
        public int deleteById(Long id) {
            dates.removeIf(d -> d.getId().equals(id));
            return 1;
        }

        @Override
        public boolean existsByPlayDay(LocalDate playDay) {
            return dates.stream().anyMatch(d -> d.getPlayDay().equals(playDay));
        }
    }

    private static class FakeReservationTimeRepository implements ReservationTimeRepository {

        private final List<ReservationTime> times = new ArrayList<>();
        private Long idCounter = 1L;

        @Override
        public Optional<ReservationTime> findById(Long id) {
            return times.stream().filter(t -> t.getId().equals(id)).findFirst();
        }

        @Override
        public ReservationTime save(ReservationTime reservationTime) {
            ReservationTime saved = ReservationTime.of(idCounter++, reservationTime.getStartAt());
            times.add(saved);
            return saved;
        }

        @Override
        public List<ReservationTime> findAll() {
            return times;
        }

        @Override
        public int deleteById(Long id) {
            times.removeIf(t -> t.getId().equals(id));
            return 1;
        }

        @Override
        public boolean existsByStartAt(LocalTime startAt) {
            return times.stream().anyMatch(t -> t.getStartAt().equals(startAt));
        }
    }

    private static class FakeThemeRepository implements ThemeRepository {

        private final List<Theme> themes = new ArrayList<>();
        private Long idCounter = 1L;

        @Override
        public Optional<Theme> findById(Long id) {
            return themes.stream().filter(t -> t.getId().equals(id)).findFirst();
        }

        @Override
        public List<Theme> findAll() {
            return themes;
        }

        @Override
        public Theme save(Theme theme) {
            Theme saved = Theme.of(idCounter++, theme.getName(), theme.getContent(), theme.getUrl());
            themes.add(saved);
            return saved;
        }

        @Override
        public int deleteById(Long id) {
            themes.removeIf(t -> t.getId().equals(id));
            return 1;
        }

        @Override
        public List<Theme> findPopularThemes(int rankLimit, LocalDate startDay, LocalDate endDay) {
            return List.of();
        }
    }
}
