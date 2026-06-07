package ktb.fullstack.talktalk.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AvailabilityResponseDto {

    @JsonProperty("is_available")
    private boolean available;
}
