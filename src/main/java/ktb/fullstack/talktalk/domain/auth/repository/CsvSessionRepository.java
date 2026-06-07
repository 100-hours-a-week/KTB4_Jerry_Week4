package ktb.fullstack.talktalk.domain.auth.repository;

import ktb.fullstack.talktalk.domain.auth.domain.Session;
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
public class CsvSessionRepository implements SessionRepository {
    private static final Path FILE_PATH = Path.of("data/sessions.csv");
    private static final String DELIMITER = ",";

    private final Map<Long, Session> sessionStore = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public CsvSessionRepository() {
        try {
            Files.createDirectories(FILE_PATH.getParent());
            if (Files.notExists(FILE_PATH)) {
                Files.createFile(FILE_PATH);
            }

            long maxId = 0;
            for (String line : Files.readAllLines(FILE_PATH)) {
                if (line.isBlank()) continue;
                Session session = parseSession(line);
                sessionStore.put(session.getId(), session);
                maxId = Math.max(maxId, session.getId());
            }

            sequence.set(maxId);
        } catch (IOException e) {
            throw new UncheckedIOException("CSV 저장소 초기화 실패", e);
        }

    }

    @Override
    public synchronized Session save(Session session) {
        if (session.getId() == null) {
            long id = sequence.incrementAndGet();
            session = new Session(id, session.getUserId(), session.getRefreshToken(), session.getExpiresAt());
        }
        sessionStore.put(session.getId(), session);
        flush();
        return session;
    }

    @Override
    public Optional<Session> findByRefreshToken(String refreshToken) {
        return sessionStore.values().stream()
                .filter(s -> s.getRefreshToken().equals(refreshToken))
                .findFirst();
    }

    @Override
    public synchronized void deleteById(Long id) {
        sessionStore.remove(id);
        flush();
    }

    @Override
    public synchronized void deleteByUserId(Long userId) {
        sessionStore.values()
                .removeIf(s -> s.getUserId().equals(userId));
        flush();
    }

    /* ===== 헬퍼 메소드 ===== */

    private Session parseSession(String line) {
        StringTokenizer st = new StringTokenizer(line, DELIMITER);

        long id = Long.parseLong(st.nextToken());
        long userId = Long.parseLong(st.nextToken());
        String refreshToken = st.nextToken();
        long expiresAt = Long.parseLong(st.nextToken());

        return new Session(id, userId, refreshToken, expiresAt);
    }

    private String serializeToCsv(Session session) {
        return String.join(DELIMITER,
                String.valueOf(session.getId()),
                String.valueOf(session.getUserId()),
                session.getRefreshToken(),
                String.valueOf(session.getExpiresAt())
        );
    }

    private void flush() {
        String content = sessionStore.values().stream()
                .map(this::serializeToCsv)
                .collect(Collectors.joining(System.lineSeparator()));
        try {
            Files.writeString(FILE_PATH, content.isEmpty() ? "" : content + System.lineSeparator());
        } catch (IOException e) {
            throw new UncheckedIOException("세션 저장 실패", e);
        }
    }
}
