package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.Item;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final Map<Long, Item> itemStorage = new HashMap<>();
    private Long currentId = 0L;
    private static final String NOT_FOUND_ITEM = "Предмет не найден";

    @Override
    public List<ItemDto> findAllOwned(Long ownerId) {
        return itemStorage.values().stream()
                .filter(item -> item.getOwner().equals(ownerId))
                .map(ItemDtoMapper::toItemDto)
                .toList();
    }

    @Override
    public ItemDto findById(Long itemId) {
        if (!itemStorage.containsKey(itemId)) {
            throw new NotFoundException(NOT_FOUND_ITEM);
        }

        return ItemDtoMapper.toItemDto(itemStorage.get(itemId));
    }

    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        Item item = ItemDtoMapper.toItem(itemDto, userId);
        item.setId(++currentId);
        itemStorage.put(item.getId(), item);
        return ItemDtoMapper.toItemDto(item);
    }

    @Override
    public ItemDto update(Long id, ItemDto itemDto, Long userId) {
        if (!itemStorage.containsKey(id)) {
            throw new NotFoundException(NOT_FOUND_ITEM);
        }
        if (!itemStorage.get(id).getOwner().equals(userId)) {
            throw new ConditionsNotMetException("Пользователь не владелец предмета");
        }

        Item item = itemStorage.get(id);
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        itemStorage.put(itemDto.getId(), item);
        return ItemDtoMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemStorage.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .map(ItemDtoMapper::toItemDto)
                .toList();
    }
}
