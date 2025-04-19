package ru.practicum.shareit.core.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.core.item.persistance.entity.dto.ItemDtoMapper;
import ru.practicum.shareit.core.item.persistance.entity.model.Item;
import ru.practicum.shareit.core.item.persistance.repository.ItemRepository;
import ru.practicum.shareit.core.request.persistance.entity.dto.ItemRequestDto;
import ru.practicum.shareit.core.request.persistance.entity.dto.ItemRequestDtoMapper;
import ru.practicum.shareit.core.request.persistance.entity.model.ItemRequest;
import ru.practicum.shareit.core.request.persistance.repository.ItemRequestRepository;
import ru.practicum.shareit.core.user.persistance.entity.model.User;
import ru.practicum.shareit.core.user.persistance.repository.UserRepository;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;

    private static final String NOT_FOUND_REQUEST = "Запрос не найден";
    private static final String NOT_FOUND_USER = "Пользователь не найден";

    @Override
    public List<ItemRequestDto> findAllOwn(Long userId) {
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(userId);
        List<Item> items = itemRepository.findAllByRequestIdIn(requests.stream().map(ItemRequest::getId).toList());
        return requests.stream().map(request -> ItemRequestDtoMapper.toItemRequestDto(request,
                items.stream()
                        .filter(item -> Objects.equals(item.getRequest().getId(), request.getId()))
                        .map(ItemDtoMapper::toItemShortDto).toList())).toList();
    }

    @Override
    public List<ItemRequestDto> findAll(Long userId) {
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterIdNotOrderByCreatedDesc(userId);
        List<Item> items = itemRepository.findAllByRequestIdIn(requests.stream().map(ItemRequest::getId).toList());
        return requests.stream().map(request -> ItemRequestDtoMapper.toItemRequestDto(request,
                items.stream()
                        .filter(item -> Objects.equals(item.getRequest().getId(), request.getId()))
                        .map(ItemDtoMapper::toItemShortDto).toList())).toList();
    }

    @Override
    public ItemRequestDto findById(Long itemRequestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(itemRequestId).orElseThrow(() -> new NotFoundException(NOT_FOUND_REQUEST));
        List<Item> items = itemRepository.findAllByRequestIdIn(List.of(itemRequest.getId()));
        return ItemRequestDtoMapper.toItemRequestDto(itemRequest, items.stream().map(ItemDtoMapper::toItemShortDto).toList());
    }

    @Override
    public ItemRequestDto create(ItemRequestDto itemRequestDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(NOT_FOUND_USER));
        ItemRequest itemRequest = ItemRequestDtoMapper.toItemRequest(itemRequestDto, user);
        return ItemRequestDtoMapper.toItemRequestDto(itemRequestRepository.saveAndFlush(itemRequest));
    }
}
