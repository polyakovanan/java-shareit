package ru.practicum.shareit.core.user.persistance.entity.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import static jakarta.persistence.GenerationType.IDENTITY;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Email(message = "Email имеет некорректный формат")
    @Column(name = "email", nullable = false)
    private String email;
}
