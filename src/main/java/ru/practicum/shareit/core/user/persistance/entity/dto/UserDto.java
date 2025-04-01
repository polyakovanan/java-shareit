package ru.practicum.shareit.core.user.persistance.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Long id;
    private String name;
    @NotBlank(message = "Email не должен быть пустым")
    @Email(message = "Email имеет некорректный формат")
    private String email;
}
