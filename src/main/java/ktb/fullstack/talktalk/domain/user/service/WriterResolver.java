package ktb.fullstack.talktalk.domain.user.service;

import ktb.fullstack.talktalk.domain.user.dto.WriterDto;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WriterResolver {

    private static final WriterDto UNKNOWN_WRITER = new WriterDto(null, "알 수 없음", null);

    private final UserRepository userRepository;

    public WriterDto resolveWriter(Long userId) {

        return userRepository.findById(userId)
                .map(user -> new WriterDto(user.getId(), user.getNickname(), user.getProfileImageUrl()))
                .orElse(UNKNOWN_WRITER);
    }
}
