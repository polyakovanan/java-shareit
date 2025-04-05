package ru.practicum.shareit.core.item.persistance.entity.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.shareit.core.user.persistance.entity.model.User;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "text", nullable = false)
    private String text;

    @ManyToOne
    private Item item;

    @ManyToOne
    private User author;

    @NotNull
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
}
