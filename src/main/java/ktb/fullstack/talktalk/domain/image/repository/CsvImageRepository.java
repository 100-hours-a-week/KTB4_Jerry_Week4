package ktb.fullstack.talktalk.domain.image.repository;

import ktb.fullstack.talktalk.domain.image.domain.Image;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class CsvImageRepository implements ImageRepository {

    private static final Path FILE_PATH = Path.of("data/images.csv");
    private static final String DELIMITER = ",";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Map<Long, Image> imageStore = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public CsvImageRepository() {
        try {
            Files.createDirectories(FILE_PATH.getParent());
            if (Files.notExists(FILE_PATH)) {
                Files.createFile(FILE_PATH);
            }

            long maxId = 0;
            for (String line : Files.readAllLines(FILE_PATH)) {
                if (line.isBlank()) continue;
                Image image = parseImage(line);
                imageStore.put(image.getId(), image);
                maxId = Math.max(maxId, image.getId());
            }

            sequence.set(maxId);
        } catch (IOException e) {
            throw new UncheckedIOException("CSV 저장소 초기화 실패", e);
        }
    }

    @Override
    public synchronized Image save(Image image) {

        Image savedImage;
        if (image.getId() == null) {
            long id = sequence.incrementAndGet();
            savedImage = new Image(id, image.getFileName(), image.getCreatedAt());
        } else {
            savedImage = image;
        }

        imageStore.put(savedImage.getId(), savedImage);
        flush();
        return savedImage;
    }


    /* ===== 헬퍼 메소드 ===== */

    private Image parseImage(String line) {

        StringTokenizer st = new StringTokenizer(line, DELIMITER);

        Long id = Long.parseLong(st.nextToken());
        String fileName = st.nextToken();
        LocalDateTime createdAt = LocalDateTime.parse(st.nextToken(), FORMATTER);

        return new Image(id, fileName, createdAt);
    }

    private String serializeToCsv(Image image) {

        return String.join(DELIMITER,
                String.valueOf(image.getId()),
                image.getFileName(),
                image.getCreatedAt().format(FORMATTER)
        );
    }

    private void flush() {

        String content = imageStore.values().stream()
                .map(this::serializeToCsv)
                .collect(Collectors.joining(System.lineSeparator()));

        try {
            Files.writeString(FILE_PATH, content.isEmpty() ? "" : content + System.lineSeparator());
        } catch (IOException e) {
            throw new UncheckedIOException("이미지 저장 실패", e);
        }
    }

}
