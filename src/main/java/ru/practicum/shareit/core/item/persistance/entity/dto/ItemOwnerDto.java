package ru.practicum.shareit.core.item.persistance.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.core.booking.persistance.entity.dto.BookingOutDto;

@Data
@Builder
public class ItemOwnerDto {
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    private Boolean available;

    private BookingOutDto lastBooking;
    private BookingOutDto nextBooking;

}
