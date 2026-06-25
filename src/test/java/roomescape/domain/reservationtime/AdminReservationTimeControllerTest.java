package roomescape.domain.reservationtime;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.LoginMember;
import roomescape.auth.SessionManager;
import roomescape.domain.member.MemberRole;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/truncate.sql")
class AdminReservationTimeControllerTest {

    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private SessionManager sessionManager;
    private String sessionId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        sessionId = sessionManager.create(new LoginMember(1L, MemberRole.MANAGER));
    }

    @Test
    @DisplayName("관리자 권한으로 예약 시간을 생성한다.")
    void createReservationTime() {
        Map<String, Object> params = new HashMap<>();
        params.put("startAt", "23:00");

        RestAssured.given().log().all()
            .cookie(SessionManager.COOKIE_NAME, sessionId)
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/admin/times")
            .then().log().all()
            .statusCode(201)
            .body("startAt", is("23:00"));
    }

    @Test
    @DisplayName("관리자 권한으로 모든 예약 시간을 조회한다.")
    void getAllReservationTimes() {
        jdbcTemplate.update("insert into reservation_time(start_at) values (?)", "23:00");

        RestAssured.given().log().all()
            .cookie(SessionManager.COOKIE_NAME, sessionId)
            .when().get("/admin/times")
            .then().log().all()
            .statusCode(200)
            .body("any { it.startAt == '23:00' }", is(true));
    }

    @Test
    @DisplayName("관리자 권한으로 예약 시간을 삭제한다.")
    void deleteReservationTime() {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("insert into reservation_time(start_at) values (?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, "23:30");
            return ps;
        }, keyHolder);
        Long timeId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        RestAssured.given().log().all()
            .cookie(SessionManager.COOKIE_NAME, sessionId)
            .when().delete("/admin/times/" + timeId)
            .then().log().all()
            .statusCode(204);
    }
}
