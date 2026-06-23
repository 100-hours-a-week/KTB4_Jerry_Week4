package ktb.fullstack.talktalk.domain.user.service;

import ktb.fullstack.talktalk.domain.user.dto.WriterDto;
import ktb.fullstack.talktalk.domain.user.entity.User;
import ktb.fullstack.talktalk.domain.user.repository.ProfileImageRepository;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WriterResolver {

    private static final String IMAGE_URL_PREFIX = "/images/";
    private static final WriterDto UNKNOWN_WRITER = new WriterDto(null, "알 수 없음", null);

    private final UserRepository userRepository;
    private final ProfileImageRepository profileImageRepository;

    @Transactional(readOnly = true)
    public WriterDto resolveWriter(Long userId) {

        return userRepository.findById(userId)
                .filter(user -> user.getDeletedAt() == null)
                .map(user -> new WriterDto(
                        user.getId(),
                        user.getNickname(),
                        profileImageRepository.findByUserIdAndCurrentTrue(userId)
                                .map(pi -> IMAGE_URL_PREFIX + pi.getImage().getFileName())
                                .orElse(null)))
                .orElse(UNKNOWN_WRITER);
    }

    @Transactional(readOnly = true)
    public Map<Long, WriterDto> resolveWriters(List<Long> userIds) {

        if (userIds.isEmpty()) return Map.of();

        Map<Long, User> users = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Map<Long, String> profileUrls = profileImageRepository.findByUserIdInAndCurrentTrue(userIds).stream()
                .collect(Collectors.toMap(
                        pi -> pi.getUser().getId(),
                        pi -> IMAGE_URL_PREFIX + pi.getImage().getFileName()));

        Map<Long, WriterDto> result = new HashMap<>();
        for (Long userId: userIds) {
            User user = users.get(userId);
            if (user == null || user.getDeletedAt() != null) {
                result.put(userId, UNKNOWN_WRITER);
            } else {
                result.put(userId, new WriterDto(
                        user.getId(),
                        user.getNickname(),
                        profileUrls.get(userId)));
            }
        }

        return result;
    }
}
