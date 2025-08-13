package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.time.LocalDate;

@Slf4j
@Service
public class UserService {


    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAllUsers() {
        return userStorage.findAll();
    }

    public User createUser(User user) {
        validateUser(user, false);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя не указано, установлено имя = логин");
        }

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }

        return userStorage.create(user);
    }

    public User updateUser(User user) {
        // Проверяем, указан ли ID пользователя
        if (user.getId() == null) {
            log.warn("Не указан ID пользователя");
            throw new ValidationException("Id должен быть указан");
        }

        // Проверяем, существует ли пользователь с указанным ID
        User existingUser = getUserById(user.getId());

        // Валидация пользователя
        validateUser(user, true);

        // Обновляем поля пользователя, если они не равны null
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
        return userStorage.update(existingUser);
    }

    private void validateUser(User user, boolean isUpdate) {
        // Проверяем E-mail
        if (!isUpdate || user.getEmail() != null) {
            if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
                log.warn("{} Введен некорректный E-mail: '{}' ", isUpdate ? "Обновление" : "Создание", user.getEmail());
                throw new ValidationException("E-mail должен быть указан и содержать символ '@'");
            }
            boolean emailUsed = false;
            for (User u : userStorage.findAll()) {
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

    // Добавление друга
    public void addFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        if (friend.getFriends() == null) {
            friend.setFriends(new HashSet<>());
        }

        boolean addedToUser = user.getFriends().add(friendId);
        boolean addedToFriend = friend.getFriends().add(userId);

        if (addedToUser && addedToFriend) {
            log.info("Пользователи {} и {} теперь друзья", userId, friendId);
            userStorage.update(user);
            userStorage.update(friend);
        } else {
            log.info("Пользователи {} и {} уже являются друзьями", userId, friendId);
        }
    }

    // Удаление из друзей
    public void removeFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        boolean removedFromUser = false;
        boolean removedFromFriend = false;

        if (user.getFriends() != null) {
            removedFromUser = user.getFriends().remove(friendId);
        }
        if (friend.getFriends() != null) {
            removedFromFriend = friend.getFriends().remove(userId);
        }

        if (removedFromUser || removedFromFriend) {
            log.info("Пользователи {} и {} больше не друзья", userId, friendId);
            userStorage.update(user);
            userStorage.update(friend);
        } else {
            log.info("Пользователи {} и {} не были друзьями", userId, friendId);
        }
    }

    // Получение общего списка друзей двух пользователей
    public List<User> getCommonFriends(Long userId1, Long userId2) {
        User user1 = getUserById(userId1);
        User user2 = getUserById(userId2);

        Set<Long> friends1 = (user1.getFriends() != null) ? user1.getFriends() : new HashSet<>();
        Set<Long> friends2 = (user2.getFriends() != null) ? user2.getFriends() : new HashSet<>();

        Set<Long> commonIds = new HashSet<>(friends1);
        commonIds.retainAll(friends2);

        List<User> commonUsers = new ArrayList<>();

        for (Long id : commonIds) {
            User u = getUserById(id);
            if (u != null) {
                commonUsers.add(u);
            }
        }
        return commonUsers;
    }

    public User getUserById(Long id) {
        User u = userStorage.getById(id);
        if (u == null) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        return u;
    }

    public List<User> getFriends(Long userId) {
        User user = getUserById(userId);
        Set<Long> friendIds = user.getFriends();
        if (friendIds == null || friendIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<User> friends = new ArrayList<>();
        for (Long id : friendIds) {
            try {
                friends.add(getUserById(id));
            } catch (NotFoundException e) {
                log.warn("Пользователь с id={} не найден: {}", id, e.getMessage());
            }
        }
        return friends;
    }
}
