package ktb.fullstack.talktalk.global.resolver;

import java.security.Principal;

public record LoginUserInfo(Long userId, Long sessionId) implements Principal {

    @Override
    public String getName() {

        return String.valueOf(userId);
    }
}
