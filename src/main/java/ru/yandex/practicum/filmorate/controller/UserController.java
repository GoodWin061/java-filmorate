package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAllUsers() {
        log.info("Вывод всех пользователей");
        return users.values();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User newUser) {
        log.debug("Начало создания пользователя с email: {}", newUser.getEmail());
        // проверяем выполнение необходимых условий
        // проверка отсутствия E-mail и наличия @
        if (newUser.getEmail() == null || newUser.getEmail().isBlank() || !newUser.getEmail().contains("@")) {
            log.warn("Некорректный email: {}", newUser.getEmail());
            throw new ValidationException("E-mail должен быть указан и содержать символ '@'");
        }

        // проверка логина
        if (newUser.getLogin() == null || newUser.getLogin().isBlank() || newUser.getLogin().contains(" ")) {
            log.warn("Некорректный логин: '{}'", newUser.getLogin());
            throw new ValidationException("Логин не может быть пустым и не должен содержать пробелы");
        }

        // проверка даты рождения
        if (newUser.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения в будущем: {}", newUser.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        // Проверка уникальности email
        for (User user : users.values()) {
            if (user.getEmail().equalsIgnoreCase(newUser.getEmail())) {
                log.warn("Попытка создать пользователя с уже используемым email: {}", newUser.getEmail());
                throw new ValidationException("Этот E-mail уже используется");
            }
        }

        // Проверка, если имя не задано используем логин
        if (newUser.getName() == null || newUser.getName().isBlank()) {
            log.info("Имя пользователя не задано, устанавливаем имя равным логину: {}", newUser.getLogin());
            newUser.setName(newUser.getLogin());
        }

        // формируем дополнительные данные
        newUser.setId(getNextId());
        // сохраняем нового пользователя в памяти приложения
        users.put(newUser.getId(), newUser);
        log.info("Пользователь успешно создан: id={}, login={}", newUser.getId(), newUser.getLogin());
        return newUser;
    }

    // вспомогательный метод для генерации идентификатора нового пользователя
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        log.debug("Начало обновления пользователя с ID: {}", newUser.getId());

        if (newUser.getId() == null) {
            log.warn("Попытка обновления пользователя без указания ID");
            throw new ValidationException("Id должен быть указан");
        }

        if (!users.containsKey(newUser.getId())) {
            log.warn("Пользователь с ID {} не найден для обновления", newUser.getId());
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }

        // Проверка email (если задан)
        if (newUser.getEmail() != null) {
            String email = newUser.getEmail();
            if (email.isBlank() || !email.contains("@")) {
                log.warn("Некорректный email при обновлении: {}", email);
                throw new ValidationException("E-mail должен быть указан и содержать символ '@'");
            }
            for (User user : users.values()) {
                if (user.getEmail().equalsIgnoreCase(email) && !user.getId().equals(newUser.getId())) {
                    log.warn("Попытка обновить email на уже используемый: {}", email);
                    throw new ValidationException("Этот E-mail уже используется");
                }
            }
        }

        // Проверка login (если задан)
        if (newUser.getLogin() != null) {
            String login = newUser.getLogin();
            if (login.isBlank() || login.contains(" ")) {
                log.warn("Некорректный логин при обновлении: '{}'", login);
                throw new ValidationException("Логин не может быть пустым и не должен содержать пробелы");
            }
        }

        // Проверка birthday (если задан)
        if (newUser.getBirthday() != null) {
            if (newUser.getBirthday().isAfter(LocalDate.now())) {
                log.warn("Дата рождения в будущем при обновлении пользователя с ID {}: {}", newUser.getId(), newUser.getBirthday());
                throw new ValidationException("Дата рождения не может быть в будущем");
            }
        }

        // Все проверки пройдены — обновляем пользователя
        User oldUser = users.get(newUser.getId());

        if (newUser.getEmail() != null) {
            oldUser.setEmail(newUser.getEmail());
            log.info("Email пользователя с ID {} обновлен на {}", newUser.getId(), newUser.getEmail());
        }

        if (newUser.getLogin() != null) {
            oldUser.setLogin(newUser.getLogin());
            log.info("Логин пользователя с ID {} обновлен на {}", newUser.getId(), newUser.getLogin());
        }

        if (newUser.getBirthday() != null) {
            oldUser.setBirthday(newUser.getBirthday());
            log.info("Дата рождения пользователя с ID {} обновлена на {}", newUser.getId(), newUser.getBirthday());
        }

        // Обновление name: если null или пустой — ставим login
        if (newUser.getName() == null || newUser.getName().isBlank()) {
            oldUser.setName(oldUser.getLogin());
            log.info("Имя пользователя с ID {} не задано, установлено имя равным логину: {}", newUser.getId(), oldUser.getLogin());
        } else {
            oldUser.setName(newUser.getName());
            log.info("Имя пользователя с ID {} обновлено на {}", newUser.getId(), newUser.getName());
        }

        log.debug("Пользователь с ID {} успешно обновлен", newUser.getId());
        return oldUser;
    }
}
