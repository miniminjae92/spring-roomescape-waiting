package roomescape.domain.theme;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.theme.dto.CreateThemeCommand;
import roomescape.domain.theme.dto.ThemeResult;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.ThemeErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThemeService {

    private static final int RANK_LIMIT = 10;
    private static final int RANK_DAYS_START = 7;
    private static final int RANK_DAYS_END = 1;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public List<ThemeResult> getAllThemeForAdmin() {
        return themeRepository.findAll().stream()
            .map(ThemeResult::from)
            .toList();
    }

    public ThemeResult createTheme(CreateThemeCommand command) {
        Theme theme = themeRepository.save(
            Theme.createWithoutId(command.name(), command.content(), command.url())
        );
        return ThemeResult.from(theme);
    }

    public void deleteTheme(Long id) {
        if (reservationRepository.countByThemeId(id) > 0) {
            throw new RoomescapeException(ThemeErrorCode.THEME_IN_USE);
        }
        int deletedCount = themeRepository.deleteById(id);
        if (deletedCount == 0) {
            log.warn("삭제할 테마가 존재하지 않습니다. themeId = {}", id);
        }
    }

    public List<ThemeResult> getAllTheme() {
        return themeRepository.findAll().stream()
            .map(ThemeResult::from)
            .toList();
    }

    public List<ThemeResult> getThemeRank() {
        LocalDate today = LocalDate.now();
        LocalDate startDay = today.minusDays(RANK_DAYS_START);
        LocalDate endDay = today.minusDays(RANK_DAYS_END);
        List<Theme> populateThemes = themeRepository.findPopularThemes(RANK_LIMIT, startDay, endDay);
        return populateThemes.stream()
            .map(ThemeResult::from)
            .toList();
    }

    public Theme findById(Long id) {
        return themeRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException(ThemeErrorCode.THEME_NOT_EXIST));
    }
}
