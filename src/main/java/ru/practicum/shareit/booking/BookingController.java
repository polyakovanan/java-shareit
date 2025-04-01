package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.persistance.entity.BookingInDto;
import ru.practicum.shareit.booking.persistance.entity.BookingOutDto;
import ru.practicum.shareit.user.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final UserService userService;

    @PostMapping
    public BookingOutDto create(@Valid @RequestBody
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) BookingInDto bookingDto,
                                @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.create(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingOutDto updateStatus(@PathVariable Long bookingId,
                                      @RequestHeader("X-Sharer-User-Id") Long userId,
                                      @RequestParam Boolean approved) {
        userService.findById(userId);
        return bookingService.updateStatus(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingOutDto findById(@PathVariable Long bookingId,
                                  @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.findById(bookingId, userId);
    }

    @GetMapping
    public List<BookingOutDto> findAllByBookerAndState(@RequestParam(required = false, defaultValue = "ALL") BookingState state,
                                               @RequestHeader("X-Sharer-User-Id") Long userId) {
        userService.findById(userId);
        return bookingService.findAllByBookerAndState(state, userId);
    }

    @GetMapping("/owner")
    public List<BookingOutDto> findAllByOwnerAndState(@RequestParam(required = false, defaultValue = "ALL") BookingState state,
                                                      @RequestHeader("X-Sharer-User-Id") Long userId) {
        userService.findById(userId);
        return bookingService.findAllByOwnerAndState(state, userId);
    }
}
