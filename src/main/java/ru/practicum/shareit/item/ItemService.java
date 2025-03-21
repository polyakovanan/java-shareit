package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> findAllOwned(Long ownerId);

    ItemDto findById(Long itemId);

    ItemDto create(ItemDto itemDto, Long userId);

    ItemDto update(Long id, ItemDto itemDto, Long userId);

    List<ItemDto> search(String text);
}
