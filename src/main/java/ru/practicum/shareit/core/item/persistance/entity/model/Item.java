package ru.practicum.shareit.core.item.persistance.entity.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.core.user.persistance.entity.model.User;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "available", nullable = false)
    private Boolean available;

    @ManyToOne
    private User owner;

    @Column(name = "request_id", nullable = false)
    private Long request;
}
