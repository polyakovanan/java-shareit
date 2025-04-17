package ru.practicum.shareit.core.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.core.booking.persistance.entity.model.Booking;
import ru.practicum.shareit.core.booking.persistance.repository.BookingRepository;
import ru.practicum.shareit.core.item.persistance.entity.dto.*;
import ru.practicum.shareit.core.item.persistance.entity.model.Comment;
import ru.practicum.shareit.core.item.persistance.entity.model.Item;
import ru.practicum.shareit.core.item.persistance.repository.CommentRepository;
import ru.practicum.shareit.core.item.persistance.repository.ItemRepository;
import ru.practicum.shareit.core.request.persistance.entity.model.ItemRequest;
import ru.practicum.shareit.core.request.persistance.repository.ItemRequestRepository;
import ru.practicum.shareit.core.user.persistance.entity.model.User;
import ru.practicum.shareit.core.user.persistance.repository.UserRepository;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    // Тесты для findAllOwned
    @Test
    void findAllOwnedShouldReturnItemsWithBookingsAndComments() {
        Long ownerId = 1L;
        User owner = createUser(ownerId);
        Item item1 = createItem(1L, owner);
        Item item2 = createItem(2L, owner);

        when(itemRepository.findAllByOwnerId(ownerId)).thenReturn(List.of(item1, item2));
        when(bookingRepository.findAllByItemOwnerIdOrderByStartAsc(ownerId))
                .thenReturn(List.of(createBooking(item1), createBooking(item2)));
        when(commentRepository.findAllByItemIdIn(anyList()))
                .thenReturn(List.of(createComment(item1), createComment(item2)));

        List<ItemDto> result = itemService.findAllOwned(ownerId);

        assertEquals(2, result.size());
        verify(itemRepository).findAllByOwnerId(ownerId);
    }

    // Тесты для findById
    @Test
    void findByIdShouldReturnItemWithBookingsAndComments() {
        Long itemId = 1L;
        User owner = createUser(1L);
        Item item = createItem(itemId, owner);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(itemId)).thenReturn(List.of(createComment(item)));
        when(bookingRepository.findAllByItemIdOrderByStartAsc(itemId)).thenReturn(List.of(createBooking(item)));

        ItemDto result = itemService.findById(itemId);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
    }

    @Test
    void findByIdWithUnknownItemShouldThrowException() {
        Long itemId = 999L;
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.findById(itemId)
        );
        assertEquals("Предмет не найден", exception.getMessage());
    }

    // Тесты для create
    @Test
    void createShouldSaveNewItem() {
        Long userId = 1L;
        User owner = createUser(userId);
        ItemDto itemDto = ItemDto.builder()
                .name("Item")
                .description("Desc")
                .available(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            Item savedItem = invocation.getArgument(0);
            savedItem.setId(1L);
            return savedItem;
        });

        ItemDto result = itemService.create(itemDto, userId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void createWithRequestShouldSaveItemWithRequest() {
        Long userId = 1L;
        Long requestId = 1L;
        User owner = createUser(userId);
        ItemRequest request = new ItemRequest();
        ItemDto itemDto = ItemDto.builder()
                .name("Item")
                .description("Desc")
                .available(true)
                .requestId(requestId)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            Item savedItem = invocation.getArgument(0);
            savedItem.setId(1L);
            return savedItem;
        });

        ItemDto result = itemService.create(itemDto, userId);

        assertNotNull(result);
        verify(itemRequestRepository).findById(requestId);
    }

    @Test
    void createWithUnknownUserShouldThrowException() {
        Long userId = 999L;
        ItemDto itemDto = ItemDto.builder().build();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.create(itemDto, userId)
        );
        assertEquals("Пользователь не найден", exception.getMessage());
    }

    // Тесты для update
    @Test
    void updateShouldUpdateItemFields() {
        Long itemId = 1L;
        Long ownerId = 1L;
        User owner = createUser(ownerId);
        Item item = createItem(itemId, owner);
        ItemDto updateDto = ItemDto.builder()
                .name("NewName")
                .description("NewDesc")
                .available(false)
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.saveAndFlush(any())).thenReturn(item);

        ItemDto result = itemService.update(itemId, updateDto, ownerId);

        assertEquals("NewName", result.getName());
        assertEquals("NewDesc", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void updateWithPartialFieldsShouldUpdateOnlyProvidedFields() {
        Long itemId = 1L;
        Long ownerId = 1L;
        User owner = createUser(ownerId);
        Item item = createItem(itemId, owner);
        ItemDto updateDto = ItemDto.builder()
                .name("NewName")
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.saveAndFlush(any())).thenReturn(item);

        ItemDto result = itemService.update(itemId, updateDto, ownerId);

        assertEquals("NewName", result.getName());
        assertEquals("Description 1", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void updateWithUnknownItemShouldThrowException() {
        Long itemId = 999L;
        ItemDto updateDto = ItemDto.builder().build();
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.update(itemId, updateDto, 1L)
        );
        assertEquals("Предмет не найден", exception.getMessage());
    }

    @Test
    void updateWithNotOwnerShouldThrowException() {
        Long itemId = 1L;
        Long ownerId = 1L;
        Long otherUserId = 2L;
        User owner = createUser(ownerId);
        Item item = createItem(itemId, owner);
        ItemDto updateDto = ItemDto.builder().build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> itemService.update(itemId, updateDto, otherUserId)
        );
        assertEquals("Пользователь не владелец предмета", exception.getMessage());
    }

    // Тесты для search
    @Test
    void searchShouldReturnAvailableItems() {
        String searchText = "search";
        Item item1 = createItem(1L, createUser(1L));
        Item item2 = createItem(2L, createUser(2L));

        when(itemRepository.findAllBySearch(searchText)).thenReturn(List.of(item1, item2));

        List<ItemDto> result = itemService.search(searchText);

        assertEquals(2, result.size());
    }

    @Test
    void searchWithEmptyTextShouldReturnEmptyList() {
        List<ItemDto> result = itemService.search("");

        assertTrue(result.isEmpty());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void searchWithBlankTextShouldReturnEmptyList() {
        List<ItemDto> result = itemService.search("   ");

        assertTrue(result.isEmpty());
        verifyNoInteractions(itemRepository);
    }

    // Тесты для createComment
    @Test
    void createCommentShouldSaveComment() {
        Long itemId = 1L;
        Long userId = 1L;
        User author = createUser(userId);
        Item item = createItem(itemId, createUser(2L));
        CommentDto commentDto = CommentDto.builder().text("Comment").build();
        Booking booking = createBooking(item);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(author));
        when(bookingRepository.findByItemIdAndBookerIdAndEndBefore(eq(itemId), eq(userId), any()))
                .thenReturn(Optional.of(booking));
        when(commentRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(1L);
            return comment;
        });

        CommentDto result = itemService.createComment(itemId, commentDto, userId);

        assertNotNull(result);
        assertEquals("Comment", result.getText());
    }

    @Test
    void createCommentWithUnknownItemShouldThrowException() {
        Long itemId = 999L;
        CommentDto commentDto = CommentDto.builder().build();
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.createComment(itemId, commentDto, 1L)
        );
        assertEquals("Предмет не найден", exception.getMessage());
    }

    @Test
    void createCommentWithUnknownUserShouldThrowException() {
        Long userId = 999L;
        Item item = createItem(1L, createUser(2L));
        CommentDto commentDto = CommentDto.builder().build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.createComment(1L, commentDto, userId)
        );
        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void createCommentWithNotBookedItemShouldThrowException() {
        Long itemId = 1L;
        Long userId = 1L;
        Item item = createItem(itemId, createUser(2L));
        CommentDto commentDto = CommentDto.builder().build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(createUser(userId)));
        when(bookingRepository.findByItemIdAndBookerIdAndEndBefore(eq(itemId), eq(userId), any()))
                .thenReturn(Optional.empty());

        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> itemService.createComment(itemId, commentDto, userId)
        );
        assertEquals("Пользователь не арендовал предмет или время аренды еще не вышло", exception.getMessage());
    }

    // Вспомогательные методы
    private User createUser(Long id) {
        return User.builder()
                .id(id)
                .name("User " + id)
                .email("user" + id + "@mail.ru")
                .build();
    }

    private Item createItem(Long id, User owner) {
        return Item.builder()
                .id(id)
                .name("Item " + id)
                .description("Description " + id)
                .available(true)
                .owner(owner)
                .build();
    }

    private Booking createBooking(Item item) {
        return Booking.builder()
                .id(1L)
                .item(item)
                .booker(createUser(10L))
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .build();
    }

    private Comment createComment(Item item) {
        return Comment.builder()
                .id(1L)
                .text("Comment")
                .item(item)
                .author(createUser(20L))
                .created(LocalDateTime.now())
                .build();
    }
}