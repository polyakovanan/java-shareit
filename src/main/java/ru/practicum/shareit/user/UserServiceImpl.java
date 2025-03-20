package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoMapper;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final Map<Long, User> userStorage = new HashMap<>();
    private Long currentId = 0L;
    private static final String NOT_FOUND_USER = "Пользователь не найден";

    public UserDto findById(Long id) {
        if (!userStorage.containsKey(id)) {
            throw new NotFoundException(NOT_FOUND_USER);
        }
        return UserDtoMapper.toUserDto(userStorage.get(id));
    }

    public UserDto create(UserDto userDto) {
        User user = UserDtoMapper.toUser(userDto);
        validate(user);
        userStorage.put(++currentId, user);
        user.setId(currentId);
        return UserDtoMapper.toUserDto(user);
    }

    public UserDto update(Long id, UserDto userDto) {
        if (!userStorage.containsKey(id)) {
            throw new NotFoundException(NOT_FOUND_USER);
        }

        User user = userStorage.get(id);
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }

        validate(user);
        userStorage.put(user.getId(), user);
        return UserDtoMapper.toUserDto(user);
    }

    public void delete(Long id) {
        if (!userStorage.containsKey(id)) {
            throw new NotFoundException(NOT_FOUND_USER);
        }
        userStorage.remove(id);
    }

    private void validate(User user) throws DuplicatedDataException {
        if (userStorage.values().stream()
                .anyMatch(u -> u.getEmail().equals(user.getEmail()) && !Objects.equals(u.getId(), user.getId()))) {
            throw new DuplicatedDataException("Этот email уже используется");
        }


    }
}
