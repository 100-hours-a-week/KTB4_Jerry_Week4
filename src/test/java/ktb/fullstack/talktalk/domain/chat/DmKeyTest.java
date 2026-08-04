package ktb.fullstack.talktalk.domain.chat;

import ktb.fullstack.talktalk.domain.chat.service.DmKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DmKeyTest {

    @Test
    @DisplayName("사용자 순서와 무관하게 동일한 키를 만든다")
    void 순서_무관_동일_키() {

        assertThat(DmKey.of(1L, 99L)).isEqualTo(DmKey.of(99L, 1L));
    }

    @Test
    @DisplayName("키는 lowId:HighId 형식으로 만들어진다")
    void 키_형식_일치() {

        assertThat(DmKey.of(99L, 1L)).isEqualTo("1:99");
    }
}
