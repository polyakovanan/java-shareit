package ru.practicum.shareit.booking.persistance.entity;

import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.persistance.entity.Item;
import ru.practicum.shareit.item.persistance.entity.ItemDtoMapper;
import ru.practicum.shareit.user.persistance.entity.User;
import ru.practicum.shareit.user.persistance.entity.UserDtoMapper;

public class BookingDtoMapper {
    private BookingDtoMapper(){}

    public static Booking toBooking(BookingInDto bookingDto, Item item, User user) {
        return Booking.builder()
                .end(bookingDto.getEnd())
                .start(bookingDto.getStart())
                .status(BookingStatus.WAITING)
                .item(item)
                .booker(user)
                .build();
    }

    public static BookingOutDto toBookingDto(Booking booking) {
        return BookingOutDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(UserDtoMapper.toUserDto(booking.getBooker()))
                .item(ItemDtoMapper.toItemDto(booking.getItem()))
                .build();
    }
}
