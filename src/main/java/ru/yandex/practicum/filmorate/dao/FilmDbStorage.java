package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Repository
public class FilmDbStorage implements FilmStorage {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String SELECT_FILMS = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
            "mpa.mpa_id, mpa.name AS mpa_name " +
            "FROM film AS f " +
            "INNER JOIN mpa_rating AS mpa ON f.mpa_id = mpa.mpa_id ";

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO film (name, description, release_date, duration, mpa_id) VALUES (:name, :description, :release_date, :duration, :mpa_id)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("release_date", Date.valueOf(film.getReleaseDate()))
                .addValue("duration", film.getDuration() != null ? film.getDuration() : 0)
                .addValue("mpa_id", film.getMpa().getId());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, params, keyHolder, new String[]{"film_id"});
        film.setId(keyHolder.getKey().longValue());
        updateGenres(film.getGenres(), film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        Long id = film.getId();
        String sql = "UPDATE film SET name = :name, description = :description, release_date = :release_date, duration = :duration, mpa_id = :mpa_id " +
                "WHERE film_id = :film_id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("release_date", film.getReleaseDate())
                .addValue("duration", film.getDuration() != null ? film.getDuration() : 0)
                .addValue("mpa_id", film.getMpa().getId())
                .addValue("film_id", id);
        int updatedRows = namedParameterJdbcTemplate.update(sql, params);
        if (updatedRows == 0) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
        updateGenres(film.getGenres(), id);
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        String sql = SELECT_FILMS + "ORDER BY f.film_id";
        List<Film> films = namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource(), (rs, rowNum) -> makeFilm(rs));
        films.forEach(this::loadGenres);
        return films;
    }

    @Override
    public Film findById(Long filmId) {
        String sql = SELECT_FILMS + "WHERE f.film_id = :film_id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("film_id", filmId);
        List<Film> films = namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> makeFilm(rs));
        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
        Film film = films.get(0);
        loadGenres(film);
        return film;
    }

    public List<Film> getTopFilms(int count) {
        String sql = SELECT_FILMS + "LEFT JOIN likes ON f.film_id = likes.film_id " +
                "GROUP BY f.film_id " +
                "ORDER BY COUNT(likes.film_id) DESC " +
                "LIMIT :count";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("count", count);
        List<Film> films = namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> makeFilm(rs));
        films.forEach(this::loadGenres);
        return films;
    }


    private Film makeFilm(ResultSet rs) throws SQLException {
        Long id = rs.getLong("film_id");
        Film film = new Film();
        film.setId(id);
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getObject("duration", Integer.class));
        film.setMpa(new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")));
        // Жанры загружаются отдельно
        return film;
    }

    private void loadGenres(Film film) {
        String sql = "SELECT g.genre_id, g.name AS genre_name " +
                "FROM genre_film AS fg " +
                "INNER JOIN genre AS g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = :film_id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("film_id", film.getId());
        List<Genre> genres = namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) ->
                new Genre(rs.getInt("genre_id"), rs.getString("genre_name")));
        film.getGenres().clear();
        film.getGenres().addAll(genres);
    }

    private void updateGenres(Set<Genre> genres, Long id) {
        MapSqlParameterSource deleteParams = new MapSqlParameterSource().addValue("film_id", id);
        namedParameterJdbcTemplate.update("DELETE FROM genre_film WHERE film_id = :film_id", deleteParams);
        if (genres != null && !genres.isEmpty()) {
            String sql = "INSERT INTO genre_film (film_id, genre_id) VALUES (:film_id, :genre_id)";
            SqlParameterSource[] batchParams = genres.stream()
                    .map(genre -> new MapSqlParameterSource()
                            .addValue("film_id", id)
                            .addValue("genre_id", genre.getId()))
                    .toArray(SqlParameterSource[]::new);
            namedParameterJdbcTemplate.batchUpdate(sql, batchParams);
        }
    }
}
