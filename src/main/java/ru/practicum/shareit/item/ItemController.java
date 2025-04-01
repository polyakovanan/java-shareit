package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.persistance.entity.ItemDto;
import ru.practicum.shareit.user.UserService;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private final UserService userService;

    @GetMapping
    public List<ItemDto> findAllOwned(@RequestHeader("X-Sharer-User-Id") Long userId) {
        userService.findById(userId);
        return itemService.findAllOwned(userId);
    }

    @GetMapping("/{id}")
    public ItemDto findById(@PathVariable Long id,
                            @RequestHeader("X-Sharer-User-Id") Long userId) {
        userService.findById(userId);
        return itemService.findById(id);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text,
                                @RequestHeader("X-Sharer-User-Id") Long userId) {
        userService.findById(userId);
        return itemService.search(text);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestBody @Valid ItemDto item,
                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.create(item, userId);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@PathVariable Long id,
                          @RequestBody ItemDto item,
                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        userService.findById(userId);
        return itemService.update(id, item, userId);
    }

}
