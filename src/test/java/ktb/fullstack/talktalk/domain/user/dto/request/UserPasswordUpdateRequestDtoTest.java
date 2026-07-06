package ktb.fullstack.talktalk.domain.user.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserPasswordUpdateRequestDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private boolean hasViolation(UserPasswordUpdateRequestDto dto, String field) {

        return validator.validate(dto).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(field));
    }

    @Test
    void 유효한_비밀번호_위반_없음() {

        UserPasswordUpdateRequestDto dto = new UserPasswordUpdateRequestDto("Password123!");

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void 비밀번호_null이면_위반() {

        UserPasswordUpdateRequestDto dto = new UserPasswordUpdateRequestDto(null);

        assertTrue(hasViolation(dto, "password"));
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

        UserPasswordUpdateRequestDto dto = new UserPasswordUpdateRequestDto(invalidPassword);

        assertTrue(hasViolation(dto, "password"));
    }
}