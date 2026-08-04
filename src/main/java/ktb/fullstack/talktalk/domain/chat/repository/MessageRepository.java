package ktb.fullstack.talktalk.domain.chat.repository;

import ktb.fullstack.talktalk.domain.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
