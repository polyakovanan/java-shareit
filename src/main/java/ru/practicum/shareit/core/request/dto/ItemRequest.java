package ru.practicum.shareit.core.request.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemRequest {
    private Long id;
    private String description;
    private Long requestor;
    private LocalDateTime created;

}
