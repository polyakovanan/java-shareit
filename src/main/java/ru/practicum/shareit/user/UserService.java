package ru.practicum.shareit.user;

import ru.practicum.shareit.user.persistance.entity.UserDto;

public interface UserService {
    UserDto findById(Long id);

    UserDto create(UserDto user);

    UserDto update(Long id, UserDto user);

    void delete(Long id);
}
