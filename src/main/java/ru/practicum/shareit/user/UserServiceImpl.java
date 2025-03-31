package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.persistance.entity.User;
import ru.practicum.shareit.user.persistance.entity.UserDto;
import ru.practicum.shareit.user.persistance.entity.UserDtoMapper;
import ru.practicum.shareit.user.persistance.repository.UserRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private static final String NOT_FOUND_USER = "Пользователь не найден";

    public UserDto findById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new NotFoundException(NOT_FOUND_USER);
        }
        return UserDtoMapper.toUserDto(user.get());
    }

    public UserDto create(UserDto userDto) {
        User user = UserDtoMapper.toUser(userDto);
        validate(user);
        return UserDtoMapper.toUserDto(userRepository.saveAndFlush(user));
    }

    public UserDto update(Long id, UserDto userDto) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            throw new NotFoundException(NOT_FOUND_USER);
        }

        User user = userOptional.get();
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }

        validate(user);
        userRepository.saveAndFlush(user);
        return UserDtoMapper.toUserDto(user);
    }

    public void delete(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new NotFoundException(NOT_FOUND_USER);
        }
        userRepository.deleteById(id);
    }

    private void validate(User user) throws DuplicatedDataException {
        if (!userRepository.findAllByEmailAndIdNot(user.getEmail(), user.getId()).isEmpty()) {
            throw new DuplicatedDataException("Этот email уже используется");
        }
    }
}
