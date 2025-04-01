package ru.practicum.shareit.core.booking.persistance.entity.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.shareit.core.booking.BookingStatus;
import ru.practicum.shareit.core.item.persistance.entity.model.Item;
import ru.practicum.shareit.core.user.persistance.entity.model.User;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @FutureOrPresent(message = "Дата начала аренды не может быть раньше текущего времени")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime start;

    @NotNull
    @Future(message = "Дата конца аренды не может быть раньше текущего времени")
    @Column(name = "end_date", nullable = false)
    private LocalDateTime end;

    @NotNull
    @ManyToOne
    private Item item;

    @NotNull
    @ManyToOne
    private User booker;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;
}
