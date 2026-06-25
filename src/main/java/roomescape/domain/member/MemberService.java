package roomescape.domain.member;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.MemberResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public List<MemberResponse> getAllUsers() {
        return memberRepository.findAllByRoleOrderByNameAsc(MemberRole.USER).stream()
            .map(MemberResponse::from)
            .toList();
    }
}
