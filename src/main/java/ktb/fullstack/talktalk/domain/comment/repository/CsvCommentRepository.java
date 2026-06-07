package ktb.fullstack.talktalk.domain.comment.repository;

import ktb.fullstack.talktalk.domain.comment.domain.Comment;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class CsvCommentRepository implements CommentRepository {

    private static final Path FILE_PATH = Path.of("data/comments.csv");
    private static final String DELIMITER = ",";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Map<Long, Comment> commentStore = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public CsvCommentRepository() {
        try {
            Files.createDirectories(FILE_PATH.getParent());
            if (Files.notExists(FILE_PATH)) {
                Files.createFile(FILE_PATH);
            }

            long maxId = 0;
            for (String line : Files.readAllLines(FILE_PATH)) {
                if (line.isBlank()) continue;
                Comment comment = parseComment(line);
                commentStore.put(comment.getId(), comment);
                maxId = Math.max(maxId, comment.getId());
            }

            sequence.set(maxId);
        } catch (IOException e) {
            throw new UncheckedIOException("CSV 저장소 초기화 실패", e);
        }
    }

    @Override
    public Optional<Comment> findById(Long id) {

        return Optional.ofNullable(commentStore.get(id));
    }

    @Override
    public synchronized Comment save(Comment comment) {

        Comment savedComment;
        if (comment.getId() == null) {
            long id = sequence.incrementAndGet();
            savedComment = new Comment(
                    id, comment.getPostId(), comment.getUserId(), comment.getContent(), comment.getCreatedAt());
        } else {
            savedComment = comment;
        }

        commentStore.put(savedComment.getId(), savedComment);
        flush();
        return savedComment;
    }

    @Override
    public List<Comment> findByCursor(Long postId, Long cursor, int size) {

        return commentStore.values().stream()
                .filter(comment -> comment.getPostId().equals(postId))
                .filter(comment -> cursor == null || comment.getId() >= cursor)
                .sorted(Comparator.comparing(Comment::getId))
                .limit(size)
                .toList();
    }

    @Override
    public synchronized void deleteById(Long id) {

        commentStore.remove(id);
        flush();
    }

    @Override
    public int getCountByPostId(Long postId) {
        return (int) commentStore.values().stream()
                .filter(comment -> comment.getPostId().equals(postId))
                .count();
    }

    /* ===== 헬퍼 메소드 ===== */

    private Comment parseComment(String line) {

        StringTokenizer st = new StringTokenizer(line, DELIMITER);

        Long id = Long.parseLong(st.nextToken());
        Long postId = Long.parseLong(st.nextToken());
        Long userId = Long.parseLong(st.nextToken());
        String content = st.nextToken();
        LocalDateTime createdAt = LocalDateTime.parse(st.nextToken(), FORMATTER);

        return new Comment(id, postId, userId, content, createdAt);
    }

    private String serializeToCsv(Comment comment) {

        return String.join(DELIMITER,
                String.valueOf(comment.getId()),
                String.valueOf(comment.getPostId()),
                String.valueOf(comment.getUserId()),
                comment.getContent(),
                comment.getCreatedAt().format(FORMATTER)
        );
    }

    private void flush() {

        String content = commentStore.values().stream()
                .map(this::serializeToCsv)
                .collect(Collectors.joining(System.lineSeparator()));

        try {
            Files.writeString(FILE_PATH, content.isEmpty() ? "" : content + System.lineSeparator());
        } catch (IOException e) {
            throw new UncheckedIOException("댓글 저장 실패", e);
        }
    }
}
