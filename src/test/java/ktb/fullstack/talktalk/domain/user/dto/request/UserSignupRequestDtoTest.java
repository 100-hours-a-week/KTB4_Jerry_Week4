package ktb.fullstack.talktalk.domain.user.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserSignupRequestDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private boolean hasViolation(UserSignupRequestDto dto, String field) {

        return validator.validate(dto).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(field));
    }

    @Test
    void 유효한_입력_위반_없음() {

        UserSignupRequestDto dto = new UserSignupRequestDto(
                "aaa@aaa.aaa", "Password123!", "jerry", null);

        Set<ConstraintViolation<UserSignupRequestDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void 이메일_형식_위반() {

        UserSignupRequestDto dto = new UserSignupRequestDto(
                "not-email-format", "Password123!", "jerry", null);

        assertTrue(hasViolation(dto, "email"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Pw<8",
            "password123!",
            "PASSWORD123!",
            "Password!",
            "Password123",
            "Password123!>20words."
    })
    void 비밀번호_규칙_위반(String invalidPassword) {

        UserSignupRequestDto dto = new UserSignupRequestDto(
                "aaa@aaa.aaa", invalidPassword, "jerry", null);

        assertTrue(hasViolation(dto, "password"));
    }

    @Test
    void 닉네임_공백_포함하면_위반() {

        UserSignupRequestDto dto = new UserSignupRequestDto("aaa@aaa.aaa", "Password123!", "hi jerry", null);

        assertTrue(hasViolation(dto, "nickname"));
    }

    @Test
    void 닉네임_10자_초과하면_위반() {

        UserSignupRequestDto dto = new UserSignupRequestDto("aaa@aaa.aaa", "Paasword123!", "nickname>10", null);

        assertTrue(hasViolation(dto, "nickname"));
    }
}
