package ru.practicum.shareit.booking.persistance.entity;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.persistance.entity.ItemDto;
import ru.practicum.shareit.user.persistance.entity.UserDto;

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
