package ru.practicum.shareit.item;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Set;

public class ItemTest {
    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void itemValidatesEmptyName() {
        ItemDto item = ItemDto.builder()
                .name("")
                .description("test")
                .available(true)
                .build();
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(item);
        Assertions.assertEquals(1, violations.size(), "Не пройдена валидация на пустое название");
    }

    @Test
    void itemValidatesEmptyDescription() {
        ItemDto item = ItemDto.builder()
                .name("test")
                .description("")
                .available(true)
                .build();
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(item);
        Assertions.assertEquals(1, violations.size(), "Не пройдена валидация на пустое описание");
    }

    @Test
    void itemValidatesNullName() {
        ItemDto item = ItemDto.builder()
                .name(null)
                .description("test")
                .available(true)
                .build();
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(item);
        Assertions.assertEquals(1, violations.size(), "Не пройдена валидация на отсутствие поля название");
    }

    @Test
    void itemValidatesNullDescription() {
        ItemDto item = ItemDto.builder()
                .name("test")
                .description(null)
                .available(true)
                .build();
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(item);
        Assertions.assertEquals(1, violations.size(), "Не пройдена валидация на отсутствие поля описание");
    }

    @Test
    void itemValidatesNullAvailable() {
        ItemDto item = ItemDto.builder()
                .name("test")
                .description("test")
                .available(null)
                .build();
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(item);
        Assertions.assertEquals(1, violations.size(), "Не пройдена валидация на отсутствие поля доступность");
    }
}
