package roomescape.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    private final PasswordHasher passwordHasher = new PasswordHasher();

    @Test
    void 비밀번호를_해시하고_검증한다() {
        String encoded = passwordHasher.hash("password123!");

        assertThat(encoded).isNotEqualTo("password123!");
        assertThat(passwordHasher.matches("password123!", encoded)).isTrue();
        assertThat(passwordHasher.matches("wrong-password", encoded)).isFalse();
    }

    @Test
    void 개발용_관리자_비밀번호를_검증한다() {
        String encoded = "120000:cm9vbWVzY2FwZS1hZG1pbg==:Rsjyx2r1Hk61aHoeG2LRlF2Sjf50zb4ph6tZqqn6mCo=";

        assertThat(passwordHasher.matches("admin1234", encoded)).isTrue();
    }
}
