package ru.yandex.practicum.filmorate.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class FriendDbStorage implements FriendStorage {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final UserStorage userStorage;

    @Autowired
    public FriendDbStorage(NamedParameterJdbcTemplate namedParameterJdbcTemplate, @Qualifier("userDbStorage") UserStorage userStorage) {
        this.namedParameterJdbcTemplate  = namedParameterJdbcTemplate;
        this.userStorage = userStorage;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        String sql = "INSERT INTO friendships (user_id, friend_id, friend_request) VALUES (:userId, :friendId, true)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);
        namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = :userId AND friend_id = :friendId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);
        namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public List<User> findAllFriends(Long id) {
        String sql = "SELECT friend_id FROM friendships WHERE user_id = :userId AND friend_request = true";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("userId", id);
        List<Long> friendIds = namedParameterJdbcTemplate.queryForList(sql, params, Long.class);

        return friendIds.stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        String sql = "SELECT u.user_id, u.email, u.login, u.name, u.birthday " +
                "FROM users u " +
                "JOIN friendships f1 ON u.user_id = f1.friend_id " +
                "JOIN friendships f2 ON u.user_id = f2.friend_id " +
                "WHERE f1.user_id = :id1 AND f2.user_id = :id2 " +
                "AND f1.friend_request = TRUE AND f2.friend_request = TRUE " +
                "AND u.user_id NOT IN (:ids)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id1", id)
                .addValue("id2", otherId)
                .addValue("ids", List.of(id, otherId));

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> makeFriend(rs));
    }

    private User makeFriend(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    }
}
