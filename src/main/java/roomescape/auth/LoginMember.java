package roomescape.auth;

import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRole;

public record LoginMember(
    Long memberId,
    MemberRole role
) {

    public static LoginMember from(Member member) {
        return new LoginMember(member.getId(), member.getRole());
    }

    public boolean isManager() {
        return role == MemberRole.MANAGER;
    }
}
