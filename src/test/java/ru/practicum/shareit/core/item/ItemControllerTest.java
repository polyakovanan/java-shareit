package ru.practicum.shareit.core.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.ShareItApp;
import ru.practicum.shareit.core.booking.BookingController;
import ru.practicum.shareit.core.booking.persistance.entity.dto.BookingInDto;
import ru.practicum.shareit.core.item.persistance.entity.dto.CommentDto;
import ru.practicum.shareit.core.item.persistance.entity.dto.ItemDto;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.core.user.UserController;
import ru.practicum.shareit.core.user.persistance.entity.dto.UserDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ShareItApp.class)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ItemControllerTest {
    static int userCount = 0;
    static int itemCount = 0;

    @Autowired
    private UserController userController;

    @Autowired
    private ItemController itemController;

    @Autowired
    private BookingController bookingController;

    @Test
    void itemControllerCreatesCorrectItem() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());

        assertNotNull(itemDto.getId());
    }

    @Test
    void itemControllerDoesNotCreateItemForUnknownUser() {
        ItemDto itemDto = getItemDto(itemCount);
        NotFoundException thrown = assertThrows(NotFoundException.class,
                () -> itemController.create(itemDto, 9999L),
        "Контроллер не выбросил исключение при попытке создать вещь для несуществующего пользователя");
        assertTrue(thrown.getMessage().contains("Пользователь не найден"));
    }

    @Test
    void itemControllerFindsItemById() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());
        ItemDto foundItemDto = itemController.findById(itemDto.getId(), userDto.getId());
        assertEquals(itemDto.getId(), foundItemDto.getId());
    }

    @Test
    void itemControllerDoesNotFindItemWithWrongId() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long userId = userDto.getId();
        NotFoundException thrown = assertThrows(NotFoundException.class,
                () -> itemController.findById(9999L, userId),
                "Контроллер не выбросил исключение при попытке найти вещь по несуществующему id");
        assertTrue(thrown.getMessage().contains("Предмет не найден"));
    }

    @Test
    void itemControllerFindsAllItemsForUser() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemController.create(itemDto, userDto.getId());
        ItemDto itemDto2 = getItemDto(itemCount);
        itemController.create(itemDto2, userDto.getId());
        ItemDto itemDto3 = getItemDto(itemCount);
        itemController.create(itemDto3, userDto.getId());

        assertEquals(3, itemController.findAllOwned(userDto.getId()).size());
    }

    @Test
    void itemControllerDoesNotFindAllItemsForUnknownUser() {
        NotFoundException thrown = assertThrows(NotFoundException.class,
                () -> itemController.findAllOwned(9999L),
                "Контроллер не выбросил исключение при попытке найти вещи для несуществующего пользователя");
        assertTrue(thrown.getMessage().contains("Пользователь не найден"));
    }

    @Test
    void itemControllerUpdatesItem() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());
        itemDto.setName("Item9999");
        itemDto.setDescription("Desc9999");
        itemDto.setAvailable(false);
        ItemDto updatedItemDto = itemController.update(itemDto.getId(), itemDto, userDto.getId());
        assertEquals(itemDto, updatedItemDto);
    }

    @Test
    void itemControllerDoesNotUpdateItemWithWrongId() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long userId = userDto.getId();
        ItemDto itemDto = getItemDto(itemCount);
        NotFoundException thrown = assertThrows(NotFoundException.class,
                () -> itemController.update(9999L, itemDto, userId),
                "Контроллер не выбросил исключение при попытке обновить вещь по несуществующему id");
        assertTrue(thrown.getMessage().contains("Предмет не найден"));
    }

    @Test
    void itemControllerDoesNotUpdateItemWithWrongUserId() {
        ItemDto itemDto = getItemDto(itemCount);
        Long itemId = itemDto.getId();
        NotFoundException thrown = assertThrows(NotFoundException.class,
                () -> itemController.update(itemId, itemDto, 9999L),
                "Контроллер не выбросил исключение при попытке обновить вещь по несуществующему пользователю");
        assertTrue(thrown.getMessage().contains("Пользователь не найден"));
    }

    @Test
    void itemControllerDoesNotUpdateItemForOtherUser() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        ItemDto createdItemDto = itemController.create(itemDto, userDto.getId());
        Long itemId = createdItemDto.getId();
        UserDto userDto2 = getUserDto(userCount);
        userDto2 = userController.create(userDto2);
        Long userId = userDto2.getId();
        ConditionsNotMetException thrown = assertThrows(ConditionsNotMetException.class,
                () -> itemController.update(itemId, createdItemDto, userId),
                "Контроллер не выбросил исключение при попытке обновить вещь другого пользователя");
        assertTrue(thrown.getMessage().contains("Пользователь не владелец предмета"));
    }

    @Test
    void itemControllerSearchesItemsByText() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemDto.setName("SearchItem");
        itemController.create(itemDto, userDto.getId());

        ItemDto itemDto2 = getItemDto(itemCount);
        itemDto2.setDescription("SearchItemDescription");
        itemController.create(itemDto2, userDto.getId());

        ItemDto itemDto3 = getItemDto(itemCount);
        itemDto3.setDescription("SearchItem");
        itemDto3.setDescription("SearchItemDescription");
        itemDto3.setAvailable(false);
        itemController.create(itemDto3, userDto.getId());

        ItemDto itemDto4 = getItemDto(itemCount);
        itemController.create(itemDto4, userDto.getId());

        assertEquals(2, itemController.search("SearchItem", userDto.getId()).size(), "Неверное количество найденных вещей");

    }

    @Test
    void itemControllerReturnsEmptyListForEmptyQuery() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        assertEquals(0, itemController.search("", userDto.getId()).size(), "Неверное количество найденных вещей");
    }

    @Test
    void itemControllerCreatesCommentForItem() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());

        userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        BookingInDto bookingDto = getBookingDto(itemDto, LocalDateTime.now().minusDays(1), LocalDateTime.now());
        bookingController.create(bookingDto, userDto.getId());

        CommentDto commentDto = getCommentDto("Comment");

        CommentDto resultCommentDto = itemController.createComment(itemDto.getId(), commentDto, userDto.getId());
        assertEquals(commentDto.getText(), resultCommentDto.getText());
        assertEquals(userDto.getName(), resultCommentDto.getAuthorName());
    }

    @Test
    void itemControllerDoesNotCreateCommentForUnknownUser() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());
        Long itemId = itemDto.getId();

        CommentDto commentDto = getCommentDto("Comment");

        NotFoundException thrown = assertThrows(NotFoundException.class,
                () -> itemController.createComment(itemId, commentDto, 9999L));
        assertTrue(thrown.getMessage().contains("Пользователь не найден"));
    }

    @Test
    void itemControllerDoesNotCreateCommentForUnknownItem() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long userId = userDto.getId();
        CommentDto commentDto = getCommentDto("Comment");

        NotFoundException thrown = assertThrows(NotFoundException.class,
                () -> itemController.createComment(9999L, commentDto, userId));
        assertTrue(thrown.getMessage().contains("Предмет не найден"));
    }

    @Test
    void itemControllerDoesNotCreateCommentForNotBookedItem() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());
        Long itemId = itemDto.getId();

        userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long userId = userDto.getId();

        CommentDto commentDto = getCommentDto("Comment");

        ConditionsNotMetException thrown = assertThrows(ConditionsNotMetException.class,
                () -> itemController.createComment(itemId, commentDto, userId));
        assertTrue(thrown.getMessage().contains("Пользователь не арендовал предмет или время аренды еще не вышло"));
    }

    @Test
    void itemControllerDoesNotCreateCommentForNotEndedBooking() {
        UserDto userDto = getUserDto(userCount);
        userDto = userController.create(userDto);

        ItemDto itemDto = getItemDto(itemCount);
        itemDto = itemController.create(itemDto, userDto.getId());
        Long itemId = itemDto.getId();

        userDto = getUserDto(userCount);
        userDto = userController.create(userDto);
        Long userId = userDto.getId();

        BookingInDto bookingDto = getBookingDto(itemDto, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        bookingController.create(bookingDto, userId);

        CommentDto commentDto = getCommentDto("Comment");

        ConditionsNotMetException thrown = assertThrows(ConditionsNotMetException.class,
                () -> itemController.createComment(itemId, commentDto, userId));
        assertTrue(thrown.getMessage().contains("Пользователь не арендовал предмет или время аренды еще не вышло"));
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

    private CommentDto getCommentDto(String text) {
        return CommentDto.builder()
                .text(text)
                .build();
    }
}
