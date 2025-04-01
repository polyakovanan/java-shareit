package ru.practicum.shareit.core.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.core.booking.persistance.entity.model.Booking;
import ru.practicum.shareit.core.booking.persistance.entity.dto.BookingInDto;
import ru.practicum.shareit.core.booking.persistance.entity.dto.BookingOutDto;
import ru.practicum.shareit.core.booking.persistance.entity.dto.BookingDtoMapper;
import ru.practicum.shareit.core.booking.persistance.repository.BookingRepository;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.core.item.persistance.entity.model.Item;
import ru.practicum.shareit.core.item.persistance.repository.ItemRepository;
import ru.practicum.shareit.core.user.persistance.entity.model.User;
import ru.practicum.shareit.core.user.persistance.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private static final String NOT_FOUND_BOOKING = "Бронирование не найдено";
    private static final String NOT_FOUND_ITEM = "Предмет не найден";
    private static final String NOT_FOUND_USER = "Пользователь не найден";

    @Override
    public BookingOutDto create(BookingInDto bookingDto, Long userId) {
        Optional<Item> item = itemRepository.findById(bookingDto.getItemId());
        Optional<User> booker = userRepository.findById(userId);

        Booking booking = BookingDtoMapper.toBooking(bookingDto,
                item.orElseThrow(() -> new NotFoundException(NOT_FOUND_ITEM)),
                booker.orElseThrow(() -> new NotFoundException(NOT_FOUND_USER)));
        validate(booking);
        booking = bookingRepository.saveAndFlush(booking);

        return BookingDtoMapper.toBookingDto(booking);
    }

    @Override
    public BookingOutDto updateStatus(Long bookingId, Long userId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_BOOKING));
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ConditionsNotMetException("Только владелец предмета может менять статус бронирования.");
        }
        booking.setStatus(Boolean.TRUE.equals(approved) ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingDtoMapper.toBookingDto(bookingRepository.saveAndFlush(booking));
    }

    @Override
    public BookingOutDto findById(Long bookingId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(NOT_FOUND_USER));
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException(NOT_FOUND_BOOKING));
        if (!booking.getBooker().getId().equals(user.getId()) &&
            !booking.getItem().getOwner().getId().equals(user.getId())) {
            throw new ConditionsNotMetException("Только владелец предмета или бронирующий может получить информацию о бронировании.");
        }
        return BookingDtoMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingOutDto> findAllByBookerAndState(BookingState state, Long userId) {
        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findAllByBookerIdOrderByStartAsc(userId);
            case WAITING -> bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED ->
                    bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            case PAST -> bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
            case CURRENT ->
                    bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, LocalDateTime.now(), LocalDateTime.now());
            case FUTURE -> bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
        };
        return bookings.stream()
                .map(BookingDtoMapper::toBookingDto)
                .toList();
    }

    @Override
    public List<BookingOutDto> findAllByOwnerAndState(BookingState state, Long userId) {
        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findAllByItemOwnerIdOrderByStartAsc(userId);
            case WAITING -> bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED ->
                    bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            case PAST -> bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
            case CURRENT -> bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, LocalDateTime.now(), LocalDateTime.now());
            case FUTURE -> bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
        };
        return bookings.stream()
                .map(BookingDtoMapper::toBookingDto)
                .toList();
    }

    private void validate(Booking booking) {
        Optional<Booking> bookingOptional = bookingRepository.findByItemIdAndEndIsAfterAndStartIsBefore(booking.getItem().getId(), booking.getEnd(), booking.getStart());
        if (bookingOptional.isPresent()) {
            throw new ConditionsNotMetException("Предмет уже забронирован на это время.");
        }
        if (booking.getBooker().getId().equals(booking.getItem().getOwner().getId())) {
            throw new ConditionsNotMetException("Владелец предмета не может арендовать его сам.");
        }
        if (Boolean.FALSE.equals(booking.getItem().getAvailable())){
            throw new ConditionsNotMetException("Предмет недоступен для аренды.");
        }
        if (booking.getEnd().isBefore(booking.getStart())) {
            throw new ConditionsNotMetException("Время окончания бронирования должно быть после времени начала.");
        }
    }
}
