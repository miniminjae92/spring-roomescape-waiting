package roomescape.auth;

import java.time.Duration;
import java.util.Optional;

public interface SessionStore {

    String create(LoginMember loginMember, Duration ttl);

    Optional<LoginMember> find(String sessionId);

    void delete(String sessionId);
}
