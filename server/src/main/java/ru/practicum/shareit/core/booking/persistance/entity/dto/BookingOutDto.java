package ru.practicum.shareit.core.booking.persistance.entity.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.core.booking.BookingStatus;
import ru.practicum.shareit.core.item.persistance.entity.dto.ItemDto;
import ru.practicum.shareit.core.user.persistance.entity.dto.UserDto;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingOutDto {
    private Long id;
    private ItemDto item;
    private UserDto booker;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;
}
