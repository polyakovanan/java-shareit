package ru.practicum.shareit.core.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.ShareItApp;
import ru.practicum.shareit.core.booking.persistance.entity.dto.BookingInDto;
import ru.practicum.shareit.core.booking.persistance.entity.dto.BookingOutDto;
import ru.practicum.shareit.core.item.ItemController;
import ru.practicum.shareit.core.item.persistance.entity.dto.ItemDto;
import ru.practicum.shareit.core.user.UserController;
import ru.practicum.shareit.core.user.persistance.entity.dto.UserDto;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ShareItApp.class)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BookingControllerTest {
    static int userCount = 0;
    static int itemCount = 0;

    @Autowired
    private UserController userController;

    @Autowired
    private ItemController itemController;

    @Autowired
    private BookingController bookingController;

    @Test
    void bookingControllerCreatesCorrectBooking() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());

        userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long userId = userDto.getId();

        BookingInDto bookingDto = getBookingDto(itemDto, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        BookingOutDto resultBookingDto = bookingController.create(bookingDto, userId);
        assertEquals(bookingDto.getItemId(), resultBookingDto.getItem().getId(), "Контроллер бронирований создал неверную бронь");
        assertEquals(userId, resultBookingDto.getBooker().getId(), "Контроллер бронирований создал неверную бронь");
    }

    @Test
    void bookingControllerDoesNotCreateBookingForUnknownUser() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());

        BookingInDto bookingDto = getBookingDto(itemDto, LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        NotFoundException thrown = assertThrows(NotFoundException.class,
                () -> bookingController.create(bookingDto, 9999L));
        assertTrue(thrown.getMessage().contains("Пользователь не найден"));
    }

    @Test
    void bookingControllerDoesNotCreateBookingForUnknownItem() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long userId = userDto.getId();

        ItemDto itemDto = getItemDto(itemCount);
        itemDto.setId(9999L);

        BookingInDto bookingDto = getBookingDto(itemDto, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        NotFoundException thrown = assertThrows(NotFoundException.class,
                () -> bookingController.create(bookingDto, userId));
        assertTrue(thrown.getMessage().contains("Предмет не найден"));
    }

    @Test
    void bookingControllerDoesNotCreateBookingForAlreadyBookedItem() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemDto.setAvailable(false);
        itemDto = itemController.create(itemDto, userDto.getId());

        userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long userId = userDto.getId();

        BookingInDto bookingDto = getBookingDto(itemDto, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        ConditionsNotMetException thrown = assertThrows(ConditionsNotMetException.class,
                () -> bookingController.create(bookingDto, userId));
        assertTrue(thrown.getMessage().contains("Предмет недоступен для аренды."));
    }

    @Test
    void bookingControllerDoesNotCreateBookingForWrongDate() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());

        userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long userId = userDto.getId();

        BookingInDto bookingDto = getBookingDto(itemDto, LocalDateTime.now().plusDays(1), LocalDateTime.now());
        ConditionsNotMetException thrown = assertThrows(ConditionsNotMetException.class,
                () -> bookingController.create(bookingDto, userId));
        assertTrue(thrown.getMessage().contains("Время окончания бронирования должно быть после времени начала."));
    }

    @Test
    void bookingControllerDoesNotCreateBookingForItemOwner() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long ownerId = userDto.getId();

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());

        BookingInDto bookingDto = getBookingDto(itemDto, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        ConditionsNotMetException thrown = assertThrows(ConditionsNotMetException.class,
                () -> bookingController.create(bookingDto, ownerId));
        assertTrue(thrown.getMessage().contains("Владелец предмета не может арендовать его сам."));
    }

    @Test
    void bookingControllerDoesNotCreateBookingForBookedDates() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());

        userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long userId = userDto.getId();

        BookingInDto bookingDto = getBookingDto(itemDto, LocalDateTime.now(), LocalDateTime.now().plusDays(2));
        bookingController.create(bookingDto, userId);

        userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long newUserId = userDto.getId();

        BookingInDto newBookingDto = getBookingDto(itemDto, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3));
        ConditionsNotMetException thrown = assertThrows(ConditionsNotMetException.class,
                () -> bookingController.create(newBookingDto, newUserId));
        assertTrue(thrown.getMessage().contains("Предмет уже забронирован на это время."));

    }

    @Test
    void bookingControllerAcceptsBookingCorrectly() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long ownerId = userDto.getId();

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());

        userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long userId = userDto.getId();

        BookingInDto bookingDto = getBookingDto(itemDto, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        BookingOutDto resultBookingDto = bookingController.create(bookingDto, userId);

        resultBookingDto = bookingController.updateStatus(resultBookingDto.getId(), ownerId, true);
        assertEquals(BookingStatus.APPROVED, resultBookingDto.getStatus(), "Контроллер бронирований не принял бронь корректно");
    }

    @Test
    void bookingControllerRejectsBookingCorrectly() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long ownerId = userDto.getId();

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());

        userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long userId = userDto.getId();

        BookingInDto bookingDto = getBookingDto(itemDto, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        BookingOutDto resultBookingDto = bookingController.create(bookingDto, userId);

        resultBookingDto = bookingController.updateStatus(resultBookingDto.getId(), ownerId, false);
        assertEquals(BookingStatus.REJECTED, resultBookingDto.getStatus(), "Контроллер бронирований не отклонил бронь корректно");
    }

    @Test
    void bookingControllerDoesNotUpdateStatusForUnknownBooking() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long ownerId = userDto.getId();

        NotFoundException thrown = assertThrows(NotFoundException.class,
                () -> bookingController.updateStatus(9999L, ownerId, true));
        assertTrue(thrown.getMessage().contains("Бронирование не найдено"));
    }

    @Test
    void bookingControllerGetsBookingForUserCorrectly() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());

        userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long userId = userDto.getId();

        BookingInDto bookingDto = getBookingDto(itemDto, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        BookingOutDto resultBookingDto = bookingController.create(bookingDto, userId);

        BookingOutDto foundBookingDto = bookingController.findById(resultBookingDto.getId(), userId);
        assertEquals(resultBookingDto.getId(), foundBookingDto.getId(), "Контроллер бронирований не вернул корректную бронь");
    }

    @Test
    void bookingControllerGetsBookingsForOwnerCorrectly() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long ownerId = userDto.getId();

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());

        userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long userId = userDto.getId();

        BookingInDto bookingDto = getBookingDto(itemDto, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        BookingOutDto resultBookingDto = bookingController.create(bookingDto, userId);

        BookingOutDto foundBookingDto = bookingController.findById(resultBookingDto.getId(), ownerId);
        assertEquals(resultBookingDto.getId(), foundBookingDto.getId(), "Контроллер бронирований не вернул корректную бронь");
    }

    @Test
    void bookingControllerDoesNotGetBookingForNotOwnerOrBooker() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());

        userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long userId = userDto.getId();

        BookingInDto bookingDto = getBookingDto(itemDto, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        BookingOutDto resultBookingDto = bookingController.create(bookingDto, userId);
        Long bookingId = resultBookingDto.getId();

        userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long newUserId = userDto.getId();

        ConditionsNotMetException thrown = assertThrows(ConditionsNotMetException.class,
                () -> bookingController.findById(bookingId, newUserId));
        assertTrue(thrown.getMessage().contains("Только владелец предмета или бронирующий может получить информацию о бронировании."));
    }

    @Test
    void bookingControllerDoesNotUpdateStatusForNotOwner() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());

        userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long userId = userDto.getId();

        BookingInDto bookingDto = getBookingDto(itemDto, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        BookingOutDto resultBookingDto = bookingController.create(bookingDto, userId);
        Long bookingId = resultBookingDto.getId();

        ConditionsNotMetException thrown = assertThrows(ConditionsNotMetException.class,
                () -> bookingController.updateStatus(bookingId, userId, true));
        assertTrue(thrown.getMessage().contains("Только владелец предмета может менять статус бронирования."));
    }

    @Test
    void bookingControllerGetsBookingListByStateForUserCorrectly() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long ownerId = userDto.getId();

        ItemDto item1 = getItemDto(itemCount);
        item1 = itemController.create(item1, userDto.getId());

        ItemDto item2 = getItemDto(itemCount);
        item2 = itemController.create(item2, userDto.getId());

        ItemDto item3 = getItemDto(itemCount);
        item3 = itemController.create(item3, userDto.getId());

        userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long userId = userDto.getId();

        // Прошлые бронирования (PAST)
        BookingInDto bookingDto1 = getBookingDto(item1, LocalDateTime.of(2023, 1, 1, 10, 0), LocalDateTime.of(2023, 1, 2, 12, 0));
        BookingOutDto resultBookingDto1 = bookingController.create(bookingDto1, userId);
        resultBookingDto1 = bookingController.updateStatus(resultBookingDto1.getId(), ownerId, true);

        BookingInDto bookingDto2 = getBookingDto(item2, LocalDateTime.of(2023, 2, 1, 10, 0), LocalDateTime.of(2023, 2, 5, 12, 0));
        BookingOutDto resultBookingDto2 = bookingController.create(bookingDto2, userId);
        resultBookingDto2 = bookingController.updateStatus(resultBookingDto2.getId(), ownerId, false);

        // Текущее бронирование (CURRENT)
        BookingInDto bookingDto3 = getBookingDto(item1, LocalDateTime.of(2023, 1, 1, 10, 0), LocalDateTime.of(2030, 1, 2, 12, 0));
        BookingOutDto resultBookingDto3 = bookingController.create(bookingDto3, userId);
        resultBookingDto3 = bookingController.updateStatus(resultBookingDto3.getId(), ownerId, true);

        // Будущие бронирования (FUTURE)
        BookingInDto bookingDto4 = getBookingDto(item2, LocalDateTime.of(2030, 1, 1, 10, 0), LocalDateTime.of(2030, 1, 2, 12, 0));
        BookingOutDto resultBookingDto4 = bookingController.create(bookingDto4, userId);
        resultBookingDto4 = bookingController.updateStatus(resultBookingDto4.getId(), ownerId, true);

        BookingInDto bookingDto5 = getBookingDto(item3, LocalDateTime.of(2030, 2, 1, 10, 0), LocalDateTime.of(2030, 2, 5, 12, 0));
        BookingOutDto resultBookingDto5 = bookingController.create(bookingDto5, userId);

        List<BookingOutDto> bookingList = bookingController.findAllByBookerAndState(BookingState.ALL, userId);
        assertEquals(5, bookingList.size());

        bookingList = bookingController.findAllByBookerAndState(BookingState.WAITING, userId);
        assertEquals(1, bookingList.size());
        assertEquals(resultBookingDto5.getId(), bookingList.getFirst().getId());

        bookingList = bookingController.findAllByBookerAndState(BookingState.REJECTED, userId);
        assertEquals(1, bookingList.size());
        assertEquals(resultBookingDto2.getId(), bookingList.getFirst().getId());

        bookingList = bookingController.findAllByBookerAndState(BookingState.PAST, userId);
        assertEquals(2, bookingList.size());
        assertEquals(resultBookingDto2.getId(), bookingList.getFirst().getId());
        assertEquals(resultBookingDto1.getId(), bookingList.get(1).getId());

        bookingList = bookingController.findAllByBookerAndState(BookingState.CURRENT, userId);
        assertEquals(1, bookingList.size());
        assertEquals(resultBookingDto3.getId(), bookingList.getFirst().getId());

        bookingList = bookingController.findAllByBookerAndState(BookingState.FUTURE, userId);
        assertEquals(2, bookingList.size());
        assertEquals(resultBookingDto5.getId(), bookingList.getFirst().getId());
        assertEquals(resultBookingDto4.getId(), bookingList.get(1).getId());
    }

    @Test
    void bookingControllerGetsBookingListByStateForOwnerCorrectly() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long ownerId = userDto.getId();

        ItemDto item1 = getItemDto(itemCount);
        item1 = itemController.create(item1, userDto.getId());

        ItemDto item2 = getItemDto(itemCount);
        item2 = itemController.create(item2, userDto.getId());

        ItemDto item3 = getItemDto(itemCount);
        item3 = itemController.create(item3, userDto.getId());

        userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long otherOwner = userDto.getId();

        ItemDto item4 = getItemDto(itemCount);
        item4 = itemController.create(item4, otherOwner);

        userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long userId = userDto.getId();

        // Прошлые бронирования (PAST)
        BookingInDto bookingDto1 = getBookingDto(item1, LocalDateTime.of(2023, 1, 1, 10, 0), LocalDateTime.of(2023, 1, 2, 12, 0));
        BookingOutDto resultBookingDto1 = bookingController.create(bookingDto1, userId);
        resultBookingDto1 = bookingController.updateStatus(resultBookingDto1.getId(), ownerId, true);

        BookingInDto bookingDto2 = getBookingDto(item2, LocalDateTime.of(2023, 2, 1, 10, 0), LocalDateTime.of(2023, 2, 5, 12, 0));
        BookingOutDto resultBookingDto2 = bookingController.create(bookingDto2, userId);
        resultBookingDto2 = bookingController.updateStatus(resultBookingDto2.getId(), ownerId, false);

        // Текущее бронирование (CURRENT)
        BookingInDto bookingDto3 = getBookingDto(item1, LocalDateTime.of(2023, 1, 1, 10, 0), LocalDateTime.of(2030, 1, 2, 12, 0));
        BookingOutDto resultBookingDto3 = bookingController.create(bookingDto3, userId);
        resultBookingDto3 = bookingController.updateStatus(resultBookingDto3.getId(), ownerId, true);

        // Будущие бронирования (FUTURE)
        BookingInDto bookingDto4 = getBookingDto(item2, LocalDateTime.of(2030, 1, 1, 10, 0), LocalDateTime.of(2030, 1, 2, 12, 0));
        BookingOutDto resultBookingDto4 = bookingController.create(bookingDto4, userId);
        resultBookingDto4 = bookingController.updateStatus(resultBookingDto4.getId(), ownerId, true);

        BookingInDto bookingDto5 = getBookingDto(item3, LocalDateTime.of(2030, 2, 1, 10, 0), LocalDateTime.of(2030, 2, 5, 12, 0));
        BookingOutDto resultBookingDto5 = bookingController.create(bookingDto5, userId);

        // Бронирование у другого владельца
        BookingInDto bookingDto6 = getBookingDto(item4, LocalDateTime.of(2030, 3, 1, 10, 0), LocalDateTime.of(2030, 3, 5, 12, 0));
        BookingOutDto resultBookingDto6 = bookingController.create(bookingDto6, userId);
        bookingController.updateStatus(resultBookingDto6.getId(), otherOwner, true);

        List<BookingOutDto> bookingList = bookingController.findAllByOwnerAndState(BookingState.ALL, ownerId);
        assertEquals(5, bookingList.size());

        bookingList = bookingController.findAllByOwnerAndState(BookingState.WAITING, ownerId);
        assertEquals(1, bookingList.size());
        assertEquals(resultBookingDto5.getId(), bookingList.getFirst().getId());

        bookingList = bookingController.findAllByOwnerAndState(BookingState.REJECTED, ownerId);
        assertEquals(1, bookingList.size());
        assertEquals(resultBookingDto2.getId(), bookingList.getFirst().getId());

        bookingList = bookingController.findAllByOwnerAndState(BookingState.PAST, ownerId);
        assertEquals(2, bookingList.size());
        assertEquals(resultBookingDto2.getId(), bookingList.getFirst().getId());
        assertEquals(resultBookingDto1.getId(), bookingList.get(1).getId());

        bookingList = bookingController.findAllByOwnerAndState(BookingState.CURRENT, ownerId);
        assertEquals(1, bookingList.size());
        assertEquals(resultBookingDto3.getId(), bookingList.getFirst().getId());

        bookingList = bookingController.findAllByOwnerAndState(BookingState.FUTURE, ownerId);
        assertEquals(2, bookingList.size());
        assertEquals(resultBookingDto5.getId(), bookingList.getFirst().getId());
        assertEquals(resultBookingDto4.getId(), bookingList.get(1).getId());
    }

    private UserDto getUserDto(int count) {
        userCount++;
        return UserDto.builder()
                .name("User" + count)
                .email("user" + count + "@mail.ru")
                .build();
    }

    private ItemDto getItemDto(int count) {
        itemCount++;
        return ItemDto.builder()
                .name("Item" + count)
                .description("Description" + count)
                .available(true)
                .build();
    }

    private BookingInDto getBookingDto(ItemDto itemDto, LocalDateTime start, LocalDateTime end) {
        return BookingInDto.builder()
                .itemId(itemDto.getId())
                .start(start)
                .end(end)
                .build();
    }
}
