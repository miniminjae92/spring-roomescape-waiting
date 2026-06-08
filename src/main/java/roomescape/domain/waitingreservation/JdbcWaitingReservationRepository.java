package roomescape.domain.waitingreservation;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRank;

@Repository
public class JdbcWaitingReservationRepository implements WaitingReservationRepository {

    private static final String SELECT_COLUMNS = """
        select wr.id, wr.name, wr.status, wr.promoted_reservation_id, wr.created_at,
               rd.id as date_id, rd.play_day,
               rt.id as time_id, rt.start_at,
               th.id as theme_id, th.name as theme_name, th.content as theme_content, th.url as theme_url
        from waiting_reservation wr
        join reservation_date rd on wr.date_id = rd.id
        join reservation_time rt on wr.time_id = rt.id
        join theme th on wr.theme_id = th.id
        """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    @Autowired
    public JdbcWaitingReservationRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
            .withTableName("waiting_reservation")
            .usingColumns(
                "name", "date_id", "time_id", "theme_id", "created_at", "status",
                "promoted_reservation_id"
            )
            .usingGeneratedKeyColumns("id");
    }

    public JdbcWaitingReservationRepository(JdbcTemplate jdbcTemplate) {
        this(new NamedParameterJdbcTemplate(jdbcTemplate));
    }

    @Override
    public WaitingReservation save(WaitingReservation waiting) {
        SqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("name", waiting.getName())
            .addValue("date_id", waiting.getDate().getId())
            .addValue("time_id", waiting.getTime().getId())
            .addValue("theme_id", waiting.getTheme().getId())
            .addValue("created_at", Timestamp.valueOf(waiting.getCreatedAt()))
            .addValue("status", waiting.getStatus().name())
            .addValue("promoted_reservation_id", waiting.getPromotedReservationId());
        long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return WaitingReservation.of(
            id,
            waiting.getName(),
            waiting.getDate(),
            waiting.getTime(),
            waiting.getTheme(),
            waiting.getCreatedAt(),
            waiting.getStatus(),
            waiting.getPromotedReservationId()
        );
    }

    @Override
    public boolean existsByNameAndDateIdAndTimeIdAndThemeId(
        String name,
        long dateId,
        long timeId,
        long themeId
    ) {
        String sql = """
            select exists(
                select 1
                from waiting_reservation
                where name = :name
                  and date_id = :dateId
                  and time_id = :timeId
                  and theme_id = :themeId
                  and status = 'WAITING'
            )
            """;
        MapSqlParameterSource parameters = slotParameters(dateId, timeId, themeId)
            .addValue("name", name);
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, parameters, Boolean.class));
    }

    @Override
    public Optional<WaitingReservation> findOldestBySlot(long dateId, long timeId, long themeId) {
        String sql = SELECT_COLUMNS + """
             where wr.date_id = :dateId
               and wr.time_id = :timeId
               and wr.theme_id = :themeId
               and wr.status = 'WAITING'
             order by wr.created_at, wr.id
             limit 1
            """;
        return findOne(sql, slotParameters(dateId, timeId, themeId));
    }

    @Override
    public Optional<WaitingReservation> findOldestBySlotForUpdate(
        long dateId,
        long timeId,
        long themeId
    ) {
        String sql = SELECT_COLUMNS + """
             where wr.date_id = :dateId
               and wr.time_id = :timeId
               and wr.theme_id = :themeId
               and wr.status = 'WAITING'
             order by wr.created_at, wr.id
             limit 1
             for update
            """;
        return findOne(sql, slotParameters(dateId, timeId, themeId));
    }

    @Override
    public List<WaitingReservationWithRank> findAllByNameWithRank(String name) {
        return findByNameWithRank(name, null, null);
    }

    @Override
    public List<WaitingReservationWithRank> findUpcomingByNameWithRank(
        String name,
        LocalDate currentDate,
        LocalTime currentTime
    ) {
        return findByNameWithRank(name, currentDate, currentTime);
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate.update(
            "delete from waiting_reservation where id = :id",
            new MapSqlParameterSource("id", id)
        );
    }

    @Override
    public Optional<WaitingReservation> findById(Long id) {
        return findOne(SELECT_COLUMNS + " where wr.id = :id", new MapSqlParameterSource("id", id));
    }

    @Override
    public Optional<WaitingReservation> findByIdForUpdate(Long id) {
        return findOne(
            SELECT_COLUMNS + " where wr.id = :id for update",
            new MapSqlParameterSource("id", id)
        );
    }

    @Override
    public int cancelById(Long id) {
        String sql = """
            update waiting_reservation
            set status = 'CANCELLED'
            where id = :id and status = 'WAITING'
            """;
        return jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
    }

    @Override
    public int promote(Long id, Long reservationId) {
        String sql = """
            update waiting_reservation
            set status = 'PROMOTED', promoted_reservation_id = :reservationId
            where id = :id and status = 'WAITING'
            """;
        SqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("reservationId", reservationId);
        return jdbcTemplate.update(sql, parameters);
    }

    @Override
    public boolean existsActiveBySlot(long dateId, long timeId, long themeId) {
        String sql = """
            select exists(
                select 1
                from waiting_reservation
                where date_id = :dateId
                  and time_id = :timeId
                  and theme_id = :themeId
                  and status = 'WAITING'
            )
            """;
        return Boolean.TRUE.equals(
            jdbcTemplate.queryForObject(sql, slotParameters(dateId, timeId, themeId), Boolean.class)
        );
    }

    @Override
    public List<WaitingReservation> findAll() {
        return jdbcTemplate.query(
            SELECT_COLUMNS + " order by wr.created_at, wr.id",
            waitingReservationRowMapper()
        );
    }

    private List<WaitingReservationWithRank> findByNameWithRank(
        String name,
        LocalDate currentDate,
        LocalTime currentTime
    ) {
        String upcomingCondition = currentDate == null ? "" : """
             and (rd.play_day > :currentDate
                  or (rd.play_day = :currentDate and rt.start_at > :currentTime))
            """;
        String sql = """
            select ranked.id, ranked.name, ranked.status, ranked.promoted_reservation_id,
                   ranked.created_at, ranked.waiting_rank,
                   rd.id as date_id, rd.play_day,
                   rt.id as time_id, rt.start_at,
                   th.id as theme_id, th.name as theme_name, th.content as theme_content, th.url as theme_url
            from (
                select wr.id, wr.name, wr.date_id, wr.time_id, wr.theme_id, wr.created_at,
                       wr.status, wr.promoted_reservation_id,
                       row_number() over (
                           partition by wr.date_id, wr.time_id, wr.theme_id
                           order by wr.created_at, wr.id
                       ) as waiting_rank
                from waiting_reservation wr
                join reservation_date rd on wr.date_id = rd.id
                join reservation_time rt on wr.time_id = rt.id
                where wr.status = 'WAITING'
            """ + upcomingCondition + """
            ) ranked
            join reservation_date rd on ranked.date_id = rd.id
            join reservation_time rt on ranked.time_id = rt.id
            join theme th on ranked.theme_id = th.id
            where ranked.name = :name
            order by rd.play_day, rt.start_at, ranked.id
            """;
        MapSqlParameterSource parameters = new MapSqlParameterSource("name", name);
        if (currentDate != null) {
            parameters.addValue("currentDate", currentDate.toString());
            parameters.addValue("currentTime", currentTime.toString());
        }
        return jdbcTemplate.query(sql, parameters, waitingReservationWithRankRowMapper());
    }

    private Optional<WaitingReservation> findOne(String sql, SqlParameterSource parameters) {
        return jdbcTemplate.query(sql, parameters, waitingReservationRowMapper()).stream().findFirst();
    }

    private MapSqlParameterSource slotParameters(long dateId, long timeId, long themeId) {
        return new MapSqlParameterSource()
            .addValue("dateId", dateId)
            .addValue("timeId", timeId)
            .addValue("themeId", themeId);
    }

    private RowMapper<WaitingReservationWithRank> waitingReservationWithRankRowMapper() {
        return (rs, rowNum) -> new WaitingReservationWithRank(
            waitingReservationRowMapper().mapRow(rs, rowNum),
            rs.getLong("waiting_rank")
        );
    }

    private RowMapper<WaitingReservation> waitingReservationRowMapper() {
        return (rs, rowNum) -> WaitingReservation.of(
            rs.getLong("id"),
            rs.getString("name"),
            ReservationDate.of(rs.getLong("date_id"), LocalDate.parse(rs.getString("play_day"))),
            ReservationTime.of(rs.getLong("time_id"), LocalTime.parse(rs.getString("start_at"))),
            Theme.of(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_content"),
                rs.getString("theme_url")
            ),
            rs.getTimestamp("created_at").toLocalDateTime(),
            WaitingReservationStatus.valueOf(rs.getString("status")),
            rs.getObject("promoted_reservation_id", Long.class)
        );
    }
}
