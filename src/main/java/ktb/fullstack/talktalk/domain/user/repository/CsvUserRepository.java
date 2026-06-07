package ktb.fullstack.talktalk.domain.user.repository;

import ktb.fullstack.talktalk.domain.user.domain.User;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class CsvUserRepository implements UserRepository {

    private static final Path FILE_PATH = Path.of("data/users.csv");
    private static final String DELIMITER = ",";

    private final Map<Long, User> userStore = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public CsvUserRepository() {
        try {
            Files.createDirectories(FILE_PATH.getParent());
            if (Files.notExists(FILE_PATH)) {
                Files.createFile(FILE_PATH);
            }

            long maxId = 0;
            for (String line : Files.readAllLines(FILE_PATH)) {
                if (line.isBlank()) continue;
                User user = parseUser(line);
                userStore.put(user.getId(), user);
                maxId = Math.max(maxId, user.getId());
            }

            sequence.set(maxId);
        } catch (IOException e) {
            throw new UncheckedIOException("CSV 저장소 초기화 실패", e);
        }
    }

    @Override
    public synchronized User save(User user) {
        User savedUser;
        if (user.getId() == null) {
            long id = sequence.incrementAndGet();
            savedUser = new User(id, user.getEmail(), user.getPassword(), user.getNickname(), user.getProfileImageUrl());
        } else {
            savedUser = user;
        }

        userStore.put(savedUser.getId(), savedUser);
        flush();
        return savedUser;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userStore.values().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return userStore.values().stream()
                .anyMatch(user -> user.getNickname().equals(nickname));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userStore.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userStore.get(id));
    }

    @Override
    public synchronized void deleteById(Long id) {
        userStore.remove(id);
        flush();
    }

    /* ===== 헬퍼 메소드 ===== */

    private User parseUser(String line) {
        StringTokenizer st = new StringTokenizer(line, DELIMITER);

        long id = Long.parseLong(st.nextToken());
        String email = st.nextToken();
        String password = st.nextToken();
        String nickname = st.nextToken();
        String profileImageUrl = st.hasMoreTokens() ? st.nextToken() : null;

        return new User(id, email, password, nickname, profileImageUrl);
    }

    private String serializeToCsv(User user) {
        return String.join(DELIMITER,
                String.valueOf(user.getId()),
                user.getEmail(),
                user.getPassword(),
                user.getNickname(),
                user.getProfileImageUrl() == null ? "" : user.getProfileImageUrl()
        );
    }

    private void flush() {
        String content = userStore.values().stream()
                .map(this::serializeToCsv)
                .collect(Collectors.joining(System.lineSeparator()));

        try {
            Files.writeString(FILE_PATH, content.isEmpty() ? "" : content + System.lineSeparator());
        } catch (IOException e) {
            throw new UncheckedIOException("유저 저장 실패", e);
        }
    }
}
