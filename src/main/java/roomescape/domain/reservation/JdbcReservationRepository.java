package roomescape.domain.reservation;

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

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private static final String SELECT_COLUMNS = """
        select r.id, r.name, r.status,
               rd.id as date_id, rd.play_day,
               rt.id as time_id, rt.start_at,
               th.id as theme_id, th.name as theme_name, th.content as theme_content, th.url as theme_url
        from reservation r
        join reservation_date rd on r.date_id = rd.id
        join reservation_time rt on r.time_id = rt.id
        join theme th on r.theme_id = th.id
        """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    @Autowired
    public JdbcReservationRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
            .withTableName("reservation")
            .usingColumns("name", "date_id", "time_id", "theme_id", "status")
            .usingGeneratedKeyColumns("id");
    }

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this(new NamedParameterJdbcTemplate(jdbcTemplate));
    }

    @Override
    public Reservation save(Reservation reservation) {
        SqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("name", reservation.getName())
            .addValue("date_id", reservation.getDate().getId())
            .addValue("time_id", reservation.getTime().getId())
            .addValue("theme_id", reservation.getTheme().getId())
            .addValue("status", reservation.getStatus().name());
        long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return Reservation.of(
            id,
            reservation.getName(),
            reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme(),
            reservation.getStatus()
        );
    }

    @Override
    public List<Reservation> findAll() {
        return jdbcTemplate.query(SELECT_COLUMNS + " order by r.id", reservationRowMapper());
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate.update(
            "delete from reservation where id = :id",
            new MapSqlParameterSource("id", id)
        );
    }

    @Override
    public int countByTimeId(Long timeId) {
        return count("select count(*) from reservation where time_id = :timeId", "timeId", timeId);
    }

    @Override
    public int countByReservationDateId(Long dateId) {
        return count("select count(*) from reservation where date_id = :dateId", "dateId", dateId);
    }

    @Override
    public List<Long> findReservedTimes(Long themeId, Long dateId) {
        String sql = """
            select time_id
            from reservation
            where theme_id = :themeId
              and date_id = :dateId
              and status = 'RESERVED'
            """;
        SqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("themeId", themeId)
            .addValue("dateId", dateId);
        return jdbcTemplate.queryForList(sql, parameters, Long.class);
    }

    @Override
    public int countByThemeId(Long themeId) {
        return count("select count(*) from reservation where theme_id = :themeId", "themeId", themeId);
    }

    @Override
    public List<Reservation> findByName(String name) {
        String sql = SELECT_COLUMNS + " where r.name = :name order by rd.play_day";
        return jdbcTemplate.query(sql, new MapSqlParameterSource("name", name), reservationRowMapper());
    }

    @Override
    public List<Reservation> findUpcomingByName(String name, LocalDate currentDate, LocalTime currentTime) {
        String sql = SELECT_COLUMNS + """
             where r.name = :name
               and r.status = 'RESERVED'
               and (rd.play_day > :currentDate
                    or (rd.play_day = :currentDate and rt.start_at > :currentTime))
             order by rd.play_day, rt.start_at
            """;
        SqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("name", name)
            .addValue("currentDate", currentDate.toString())
            .addValue("currentTime", currentTime.toString());
        return jdbcTemplate.query(sql, parameters, reservationRowMapper());
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return findOne(SELECT_COLUMNS + " where r.id = :id", new MapSqlParameterSource("id", id));
    }

    @Override
    public Optional<Reservation> findByIdForUpdate(Long id) {
        return findOne(
            SELECT_COLUMNS + " where r.id = :id for update",
            new MapSqlParameterSource("id", id)
        );
    }

    @Override
    public Optional<Reservation> findActiveBySlotForUpdate(Long dateId, Long timeId, Long themeId) {
        String sql = SELECT_COLUMNS + """
             where r.date_id = :dateId
               and r.time_id = :timeId
               and r.theme_id = :themeId
               and r.status = 'RESERVED'
             for update
            """;
        return findOne(sql, slotParameters(dateId, timeId, themeId));
    }

    @Override
    public int updateReservation(Long id, Long dateId, Long timeId) {
        String sql = """
            update reservation
            set date_id = :dateId, time_id = :timeId
            where id = :id and status = 'RESERVED'
            """;
        SqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("dateId", dateId)
            .addValue("timeId", timeId);
        return jdbcTemplate.update(sql, parameters);
    }

    @Override
    public boolean existsByDateIdAndTimeIdAndThemeId(Long dateId, Long timeId, Long themeId) {
        String sql = """
            select exists(
                select 1
                from reservation
                where date_id = :dateId
                  and time_id = :timeId
                  and theme_id = :themeId
                  and status = 'RESERVED'
            )
            """;
        Boolean exists = jdbcTemplate.queryForObject(
            sql,
            slotParameters(dateId, timeId, themeId),
            Boolean.class
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public int cancelById(Long id) {
        String sql = """
            update reservation
            set status = 'CANCELLED'
            where id = :id and status = 'RESERVED'
            """;
        return jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
    }

    private int count(String sql, String parameterName, Long value) {
        Integer count = jdbcTemplate.queryForObject(
            sql,
            new MapSqlParameterSource(parameterName, value),
            Integer.class
        );
        return count == null ? 0 : count;
    }

    private Optional<Reservation> findOne(String sql, SqlParameterSource parameters) {
        return jdbcTemplate.query(sql, parameters, reservationRowMapper()).stream().findFirst();
    }

    private MapSqlParameterSource slotParameters(Long dateId, Long timeId, Long themeId) {
        return new MapSqlParameterSource()
            .addValue("dateId", dateId)
            .addValue("timeId", timeId)
            .addValue("themeId", themeId);
    }

    private RowMapper<Reservation> reservationRowMapper() {
        return (rs, rowNum) -> Reservation.of(
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
            ReservationStatus.valueOf(rs.getString("status"))
        );
    }
}
