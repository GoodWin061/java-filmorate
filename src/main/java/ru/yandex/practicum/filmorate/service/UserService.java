package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.time.LocalDate;

@Slf4j
@Service
public class UserService {

    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> findAllUsers() {
        return users.values();
    }

    public User createUser(User user) {
        validateUser(user, false);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя не указано, установлено имя = логин");
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь создан с id {}", user.getId());
        return user;
    }

    public User updateUser(User user) {
        if (user.getId() == null) {
            log.warn("Не указан ID пользователя");
            throw new ValidationException("Id должен быть указан");
        }
        if (!users.containsKey(user.getId())) {
            log.warn("Пользователь с id = '{}' не найден", user.getId());
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }

        validateUser(user, true);

        User existingUser = users.get(user.getId());

        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getLogin() != null) {
            existingUser.setLogin(user.getLogin());
        }
        if (user.getBirthday() != null) {
            existingUser.setBirthday(user.getBirthday());
        }
        if (user.getName() == null || user.getName().isBlank()) {
            existingUser.setName(existingUser.getLogin());
        } else {
            existingUser.setName(user.getName());
        }

        log.info("Пользователь с id {} обновлен", user.getId());
        return existingUser;
    }

    private void validateUser(User user, boolean isUpdate) {
        // Проверяем E-mail
        if (!isUpdate || user.getEmail() != null) {
            if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
                log.warn("{} Введен некорректный E-mail: '{}' ", isUpdate ? "Обновление" : "Создание", user.getEmail());
                throw new ValidationException("E-mail должен быть указан и содержать символ '@'");
            }
            boolean emailUsed = false;
            for (User u : users.values()) {
                if ((!isUpdate || !u.getId().equals(user.getId())) && u.getEmail().equalsIgnoreCase(user.getEmail())) {
                    emailUsed = true;
                    break;
                }
            }
            if (emailUsed) {
                log.warn("{} Введен E-mail, который уже используется: '{}' ", isUpdate ? "Обновление" : "Создание", user.getEmail());
                throw new ValidationException("Этот E-mail уже используется");
            }
        }

        // Проверяем логин
        if (!isUpdate || user.getLogin() != null) {
            if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
                log.warn("Введен некорректный логин: '{}' при {} ", isUpdate ? "обновлении" : "создании", user.getLogin());
                throw new ValidationException("Логин не может быть пустым и не должен содержать пробелы");
            }
        }

        // Проверяем дату рождения
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Введена дата рождения в будущем: '{}' при {} ", isUpdate ? "обновлении" : "создании", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private long getNextId() {
        return users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0) + 1;
    }
}
