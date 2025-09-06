package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendStorage {
    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    List<User> findAllFriends(Long id);

    List<User> getCommonFriends(Long id, Long otherId);
}
