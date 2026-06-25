package roomescape.domain.theme;

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
class AdminThemeControllerTest {

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
    @DisplayName("관리자 권한으로 테마를 생성한다.")
    void createTheme() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "새로운테마");
        params.put("content", "설명");
        params.put("url", "url");

        RestAssured.given().log().all()
            .cookie(SessionManager.COOKIE_NAME, sessionId)
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/admin/themes")
            .then().log().all()
            .statusCode(201)
            .body("name", is("새로운테마"));
    }

    @Test
    @DisplayName("관리자 권한으로 모든 테마를 조회한다.")
    void getAllThemeForAdmin() {
        jdbcTemplate.update("insert into theme(name, content, url) values (?, ?, ?)", "관리자테마", "설명", "url");

        RestAssured.given().log().all()
            .cookie(SessionManager.COOKIE_NAME, sessionId)
            .when().get("/admin/themes")
            .then().log().all()
            .statusCode(200)
            .body("any { it.name == '관리자테마' }", is(true));
    }

    @Test
    @DisplayName("관리자 권한으로 테마를 삭제한다.")
    void deleteTheme() {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "insert into theme(name, content, url) values (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, "삭제될테마");
            ps.setString(2, "설명");
            ps.setString(3, "url");
            return ps;
        }, keyHolder);
        Long themeId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        RestAssured.given().log().all()
            .cookie(SessionManager.COOKIE_NAME, sessionId)
            .when().delete("/admin/themes/" + themeId)
            .then().log().all()
            .statusCode(204);
    }
}
