package roomescape.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.MemberRole;

class InMemorySessionStoreTest {

    @Test
    void 만료되지_않은_세션을_조회한다() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-25T00:00:00Z"), ZoneOffset.UTC);
        InMemorySessionStore sessionStore = new InMemorySessionStore(clock);
        LoginMember loginMember = new LoginMember(1L, MemberRole.USER);

        String sessionId = sessionStore.create(loginMember, Duration.ofHours(2));

        assertThat(sessionStore.find(sessionId)).contains(loginMember);
    }

    @Test
    void 만료된_세션은_조회하지_않는다() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-25T00:00:00Z"), ZoneOffset.UTC);
        InMemorySessionStore sessionStore = new InMemorySessionStore(clock);
        String sessionId = sessionStore.create(new LoginMember(1L, MemberRole.USER), Duration.ZERO);

        assertThat(sessionStore.find(sessionId)).isEmpty();
    }
}
