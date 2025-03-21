package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@Builder
public class UserDto {
    private Long id;
    private String name;
    @NotBlank(message = "Email не должен быть пустым")
    @Email(message = "Email имеет некорректный формат")
    private String email;
}
