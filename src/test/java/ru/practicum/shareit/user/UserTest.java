package ru.practicum.shareit.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.persistance.entity.UserDto;

import java.util.Set;

public class UserTest {
    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void userValidatesBlankEmail() {
        UserDto user = UserDto.builder()
                .name("Тестовый пользователь")
                .email("")
                .build();
        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);
        Assertions.assertEquals(1, violations.size(), "Не пройдена валидация на пустой email");
    }

    @Test
    void userValidatesNullEmail() {
        UserDto user = UserDto.builder()
                .name("Тестовый пользователь")
                .build();
        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);
        Assertions.assertEquals(1, violations.size(), "Не пройдена валидация на null email");
    }

    @Test
    void userValidatesIncorrectEmail() {
        UserDto user = UserDto.builder()
                .name("Тестовый пользователь")
                .email("testmail.com")
                .build();
        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);
        Assertions.assertEquals(1, violations.size(), "Не пройдена валидация на некорректный email");
    }
}
