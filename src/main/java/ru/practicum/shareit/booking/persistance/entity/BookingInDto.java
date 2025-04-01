package ru.practicum.shareit.booking.persistance.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingInDto {
    @NotNull(message = "Предмет для аренды не может быть пустым")
    private Long itemId;
    @NotNull(message = "Дата начала аренды не может быть пустой")
    private LocalDateTime start;
    @NotNull(message = "Дата конца аренды не может быть пустой")
    private LocalDateTime end;
}
