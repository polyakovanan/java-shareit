package ru.practicum.shareit.core.item;

import ru.practicum.shareit.core.item.persistance.entity.dto.ItemDto;
import ru.practicum.shareit.core.item.persistance.entity.dto.ItemOwnerDto;

import java.util.List;

public interface ItemService {
    List<ItemOwnerDto> findAllOwned(Long ownerId);

    ItemDto findById(Long itemId);

    ItemDto create(ItemDto itemDto, Long userId);

    ItemDto update(Long id, ItemDto itemDto, Long userId);

    List<ItemDto> search(String text);
}
