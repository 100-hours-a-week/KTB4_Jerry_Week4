package ktb.fullstack.talktalk.domain.user.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserInfoUpdateRequestDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private boolean hasViolation(UserInfoUpdateRequestDto dto, String field) {

        return validator.validate(dto).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(field));
    }

    @Test
    void 유효한_닉네임_위반_없음() {

        UserInfoUpdateRequestDto dto = new UserInfoUpdateRequestDto("jerry", null);

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void 닉네임_빈_문자열이면_위반() {

        UserInfoUpdateRequestDto dto = new UserInfoUpdateRequestDto("", null);

        assertTrue(hasViolation(dto, "nickname"));
    }

    @Test
    void 닉네임_공백_포함하면_위반() {

        UserInfoUpdateRequestDto dto = new UserInfoUpdateRequestDto("hi jerry", null);

        assertTrue(hasViolation(dto, "nickname"));
    }

    @Test
    void 닉네임_10자_초과하면_위반() {

        UserInfoUpdateRequestDto dto = new UserInfoUpdateRequestDto("nickname>10", null);

        assertTrue(hasViolation(dto, "nickname"));
    }
}