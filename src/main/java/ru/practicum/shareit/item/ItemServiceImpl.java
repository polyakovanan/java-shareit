package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.persistance.entity.Item;
import ru.practicum.shareit.item.persistance.entity.ItemDto;
import ru.practicum.shareit.item.persistance.entity.ItemDtoMapper;
import ru.practicum.shareit.item.persistance.repository.ItemRepository;
import ru.practicum.shareit.user.persistance.entity.User;
import ru.practicum.shareit.user.persistance.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private static final String NOT_FOUND_ITEM = "Предмет не найден";

    @Override
    public List<ItemDto> findAllOwned(Long ownerId) {
        return itemRepository.findAllByOwnerId(ownerId).stream()
                .map(ItemDtoMapper::toItemDto)
                .toList();
    }

    @Override
    public ItemDto findById(Long itemId) {
        Optional<Item> item = itemRepository.findById(itemId);
        return ItemDtoMapper.toItemDto(item.orElseThrow(() -> new NotFoundException(NOT_FOUND_ITEM)));
    }

    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = ItemDtoMapper.toItem(itemDto, user);
        return ItemDtoMapper.toItemDto(itemRepository.saveAndFlush(item));
    }

    @Override
    public ItemDto update(Long id, ItemDto itemDto, Long userId) {
        Optional<Item> itemOptional = itemRepository.findById(id);
        Item item = itemOptional.orElseThrow(() -> new NotFoundException(NOT_FOUND_ITEM));

        if (!item.getOwner().getId().equals(userId)) {
            throw new ConditionsNotMetException("Пользователь не владелец предмета");
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return ItemDtoMapper.toItemDto(itemRepository.saveAndFlush(item));
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.findAllBySearch(text).stream()
                .map(ItemDtoMapper::toItemDto)
                .toList();
    }
}
