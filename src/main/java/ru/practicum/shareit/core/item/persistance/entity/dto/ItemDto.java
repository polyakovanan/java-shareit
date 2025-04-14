package ru.practicum.shareit.core.item.persistance.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.core.booking.persistance.entity.dto.BookingOutDto;

import java.util.List;

@Data
@Builder
public class ItemDto {
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    private Boolean available;

    private BookingOutDto lastBooking;
    private BookingOutDto nextBooking;
    private List<CommentDto> comments;
    private Long requestId;

}
