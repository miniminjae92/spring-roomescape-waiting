package roomescape.auth;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.MemberResponse;
import roomescape.auth.dto.SignupRequest;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.support.exception.AuthErrorCode;
import roomescape.support.exception.MemberErrorCode;
import roomescape.support.exception.RoomescapeException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordHasher passwordHasher;
    private final Clock clock;

    public MemberResponse login(LoginRequest request) {
        Member member = memberRepository.findByLoginId(request.loginId())
            .orElseThrow(() -> new RoomescapeException(AuthErrorCode.LOGIN_FAILED));
        if (!passwordHasher.matches(request.password(), member.getPasswordHash())) {
            throw new RoomescapeException(AuthErrorCode.LOGIN_FAILED);
        }
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse signup(SignupRequest request) {
        if (memberRepository.existsByLoginId(request.loginId())) {
            throw new RoomescapeException(MemberErrorCode.DUPLICATE_LOGIN_ID);
        }
        Member member = Member.createUser(
            request.loginId(),
            passwordHasher.hash(request.password()),
            request.name(),
            LocalDateTime.now(clock)
        );
        return MemberResponse.from(memberRepository.save(member));
    }

    public MemberResponse getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new RoomescapeException(MemberErrorCode.MEMBER_NOT_FOUND));
        return MemberResponse.from(member);
    }
}
