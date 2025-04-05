package ru.practicum.shareit.core.item.persistance.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    Long id;
    @NotBlank(message = "Текст отзыва не может быть пустым")
    String text;

    String authorName;

    LocalDateTime created;
}
