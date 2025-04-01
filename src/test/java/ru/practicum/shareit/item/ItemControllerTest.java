package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.ShareItApp;
import ru.practicum.shareit.core.item.ItemController;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.core.item.persistance.entity.dto.ItemDto;
import ru.practicum.shareit.core.user.UserController;
import ru.practicum.shareit.core.user.persistance.entity.dto.UserDto;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ShareItApp.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ItemControllerTest {
    static int userCount = 0;
    static int itemCount = 0;

    @Autowired
    private UserController userController;

    @Autowired
    private ItemController itemController;

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
        assertEquals(itemDto, foundItemDto);
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
}
