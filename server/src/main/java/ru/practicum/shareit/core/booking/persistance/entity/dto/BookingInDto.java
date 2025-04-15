package ru.practicum.shareit.core.booking.persistance.entity.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingInDto {
    private Long itemId;
    private LocalDateTime start;
    private LocalDateTime end;
}
