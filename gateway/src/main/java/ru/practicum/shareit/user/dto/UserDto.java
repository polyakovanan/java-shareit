package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    @NotBlank(message = "Имя не должно быть пустым")
    private String name;
    @NotBlank(message = "Email не должен быть пустым")
    @Email(message = "Email имеет некорректный формат")
    private String email;
}