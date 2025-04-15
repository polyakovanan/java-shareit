package ru.practicum.shareit.core.request.persistance.entity.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.core.item.persistance.entity.dto.ItemShortDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ItemRequestDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ItemShortDto> items;
}
