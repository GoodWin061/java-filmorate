package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.time.LocalDate;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;
    private final FriendStorage friendStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, FriendStorage friendStorage) {
        this.userStorage = userStorage;
        this.friendStorage = friendStorage;
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
        if (user.getId() == null) {
            log.warn("Не указан ID пользователя");
            throw new ValidationException("Id должен быть указан");
        }

        User existingUser = getUserById(user.getId());

        validateUser(user, true);

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

        if (!isUpdate || user.getLogin() != null) {
            if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
                log.warn("Введен некорректный логин: '{}' при {} ", isUpdate ? "обновлении" : "создании", user.getLogin());
                throw new ValidationException("Логин не может быть пустым и не должен содержать пробелы");
            }
        }

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Введена дата рождения в будущем: '{}' при {} ", isUpdate ? "обновлении" : "создании", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

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
            friendStorage.addFriend(userId, friendId);
        } else {
            log.info("Пользователи {} и {} уже являются друзьями", userId, friendId);
        }
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        log.info("Пользователи {} и {} больше не друзья", userId, friendId);

        if (user.getFriends() != null) user.getFriends().remove(friendId);
        if (friend.getFriends() != null) friend.getFriends().remove(userId);

        userStorage.update(user);
        userStorage.update(friend);
        friendStorage.removeFriend(userId, friendId);
    }

    public List<User> getCommonFriends(Long userId1, Long userId2) {
        return friendStorage.getCommonFriends(userId1, userId2);
    }

    public User getUserById(Long id) {
        User u = userStorage.getById(id);
        if (u == null) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        return u;
    }

    public List<User> getFriends(Long userId) {
        getUserById(userId);
        return friendStorage.findAllFriends(userId);
    }
}
