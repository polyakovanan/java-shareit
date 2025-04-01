package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.persistance.entity.BookingInDto;
import ru.practicum.shareit.booking.persistance.entity.BookingOutDto;

import java.util.List;

public interface BookingService {
    BookingOutDto create(BookingInDto booking, Long userId);

    BookingOutDto updateStatus(Long bookingId, Long userId, Boolean approved);

    BookingOutDto findById(Long bookingId, Long userId);

    List<BookingOutDto> findAllByBookerAndState(BookingState state, Long userId);

    List<BookingOutDto> findAllByOwnerAndState(BookingState state, Long userId);
}
