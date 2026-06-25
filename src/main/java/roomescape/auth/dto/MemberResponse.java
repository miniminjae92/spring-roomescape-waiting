package roomescape.auth.dto;

import roomescape.auth.LoginMember;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRole;

public record MemberResponse(
    Long id,
    String loginId,
    String name,
    MemberRole role
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getLoginId(), member.getName(), member.getRole());
    }

    public LoginMember toLoginMember() {
        return new LoginMember(id, role);
    }
}
