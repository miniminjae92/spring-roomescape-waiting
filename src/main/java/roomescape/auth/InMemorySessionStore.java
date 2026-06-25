package roomescape.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemorySessionStore implements SessionStore {

    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    private final Clock clock;

    public InMemorySessionStore(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String create(LoginMember loginMember, Duration ttl) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, new Session(loginMember, Instant.now(clock).plus(ttl)));
        return sessionId;
    }

    @Override
    public Optional<LoginMember> find(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            return Optional.empty();
        }
        if (session.isExpired(Instant.now(clock))) {
            sessions.remove(sessionId);
            return Optional.empty();
        }
        return Optional.of(session.loginMember());
    }

    @Override
    public void delete(String sessionId) {
        sessions.remove(sessionId);
    }

    private record Session(LoginMember loginMember, Instant expiresAt) {

        private boolean isExpired(Instant now) {
            return !expiresAt.isAfter(now);
        }
    }
}
