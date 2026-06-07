package ktb.fullstack.talktalk.domain.post.repository;

import ktb.fullstack.talktalk.domain.post.domain.Post;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class CsvPostRepository implements PostRepository {

    private static final Path FILE_PATH = Path.of("data/posts.csv");
    private static final String DELIMITER = ",";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Map<Long, Post> postStore = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public CsvPostRepository() {
        try {
            Files.createDirectories(FILE_PATH.getParent());
            if (Files.notExists(FILE_PATH)) {
                Files.createFile(FILE_PATH);
            }

            long maxId = 0;
            for (String line : Files.readAllLines(FILE_PATH)) {
                if (line.isBlank()) continue;
                Post post = parsePost(line);
                postStore.put(post.getId(), post);
                maxId = Math.max(maxId, post.getId());
            }

            sequence.set(maxId);
        } catch (IOException e) {
            throw new UncheckedIOException("CSV 저장소 초기화 실패", e);
        }
    }

    @Override
    public List<Post> findByCursor(Long cursor, int size) {
        return postStore.values().stream()
                .filter(post -> cursor == null || post.getId() <= cursor)
                .sorted(Comparator.comparing(Post::getId).reversed())
                .limit(size)
                .toList();
    }

    @Override
    public synchronized Post save(Post post) {
        Post savedPost;
        if (post.getId() == null) {
            long id = sequence.incrementAndGet();
            savedPost = new Post(id, post.getUserId(), post.getTitle(), post.getContent(),
                    post.getViewCount(), post.getCreatedAt(), post.getPostImageUrl());
        } else {
            savedPost = post;
        }

        postStore.put(savedPost.getId(), savedPost);
        flush();
        return savedPost;
    }

    @Override
    public Optional<Post> findById(Long id) {
        return Optional.ofNullable(postStore.get(id));
    }

    @Override
    public synchronized void deleteById(Long id) {
        postStore.remove(id);
        flush();
    }

    /* ===== 헬퍼 메소드 ===== */

    private Post parsePost(String line) {
        StringTokenizer st = new StringTokenizer(line, DELIMITER);

        Long id = Long.parseLong(st.nextToken());
        Long userId = Long.parseLong(st.nextToken());
        String title = st.nextToken();
        String content = st.nextToken();
        int viewCount = Integer.parseInt(st.nextToken());
        LocalDateTime createdAt = LocalDateTime.parse(st.nextToken(), FORMATTER);
        String postImageUrl = st.hasMoreTokens() ? st.nextToken() : null;

        return new Post(id, userId, title, content, viewCount, createdAt, postImageUrl);
    }

    private String serializeToCsv(Post post) {
        return String.join(DELIMITER,
                String.valueOf(post.getId()),
                String.valueOf(post.getUserId()),
                post.getTitle(),
                post.getContent(),
                String.valueOf(post.getViewCount()),
                post.getCreatedAt().format(FORMATTER),
                post.getPostImageUrl() == null ? "" : post.getPostImageUrl()
        );
    }

    private void flush() {
        String content = postStore.values().stream()
                .map(this::serializeToCsv)
                .collect(Collectors.joining(System.lineSeparator()));

        try {
            Files.writeString(FILE_PATH, content.isEmpty() ? "" : content + System.lineSeparator());
        } catch (IOException e) {
            throw new UncheckedIOException("게시글 저장 실패", e);
        }
    }
}
