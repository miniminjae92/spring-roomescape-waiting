package roomescape.domain.member;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.support.exception.MemberErrorCode;
import roomescape.support.exception.RoomescapeException;

@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "loginId"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String loginId;
    private String passwordHash;
    private String name;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    private LocalDateTime createdAt;

    private Member(
        Long id,
        String loginId,
        String passwordHash,
        String name,
        MemberRole role,
        LocalDateTime createdAt
    ) {
        validate(loginId, passwordHash, name, role, createdAt);
        this.id = id;
        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static Member createUser(String loginId, String passwordHash, String name, LocalDateTime createdAt) {
        return new Member(null, loginId, passwordHash, name, MemberRole.USER, createdAt);
    }

    public static Member of(
        Long id,
        String loginId,
        String passwordHash,
        String name,
        MemberRole role,
        LocalDateTime createdAt
    ) {
        return new Member(id, loginId, passwordHash, name, role, createdAt);
    }

    private static void validate(
        String loginId,
        String passwordHash,
        String name,
        MemberRole role,
        LocalDateTime createdAt
    ) {
        if (loginId == null || loginId.isBlank()) {
            throw new RoomescapeException(MemberErrorCode.INVALID_LOGIN_ID);
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new RoomescapeException(MemberErrorCode.INVALID_PASSWORD);
        }
        if (name == null || name.isBlank()) {
            throw new RoomescapeException(MemberErrorCode.INVALID_MEMBER_NAME);
        }
        if (role == null || createdAt == null) {
            throw new RoomescapeException(MemberErrorCode.INVALID_MEMBER);
        }
    }
}
