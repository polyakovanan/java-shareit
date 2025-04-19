package ru.practicum.shareit.core.user;

import ru.practicum.shareit.core.user.persistance.entity.dto.UserDto;

public interface UserService {
    UserDto findById(Long id);

    UserDto create(UserDto user);

    UserDto update(Long id, UserDto user);

    void delete(Long id);
}
