package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Repository
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final String SELECT_FILMS = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
            "mpa.mpa_id, mpa.name AS mpa_name " +
            "FROM film AS f " +
            "INNER JOIN mpa_rating AS mpa ON f.mpa_id = mpa.mpa_id ";

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO film (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[]{"film_id"});
                    ps.setString(1, film.getName());
                    ps.setString(2, film.getDescription());
                    ps.setDate(3, Date.valueOf(film.getReleaseDate()));
                    ps.setInt(4, film.getDuration() != null ? film.getDuration() : 0);
                    ps.setInt(5, film.getMpa().getId());
                    return ps;
                }, keyHolder);
        film.setId(keyHolder.getKey().longValue());
        updateGenres(film.getGenres(), film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        Long id = film.getId();
        String sql = "UPDATE film SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                "WHERE film_id = ?";
        int updatedRows = jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration() != null ? film.getDuration() : 0, film.getMpa().getId(), id);
        if (updatedRows == 0) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
        updateGenres(film.getGenres(), id);
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "ORDER BY f.film_id";
        List<Film> films = jdbcTemplate.query(SELECT_FILMS + sql, (rs, rowNum) -> makeFilm(rs));
        films.forEach(this::loadGenres);
        return films;
    }

    @Override
    public Film findById(Long filmId) {
        String sql = "WHERE f.film_id = ?";
        List<Film> films = jdbcTemplate.query(SELECT_FILMS + sql, (rs, rowNum) -> makeFilm(rs), filmId);
        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
        Film film = films.get(0);
        loadGenres(film);
        return film;
    }

    public List<Film> getTopFilms(int count) {
        String sql = "LEFT JOIN likes ON f.film_id = likes.film_id " +
                "GROUP BY f.film_id " +
                "ORDER BY COUNT(likes.film_id) DESC " +
                "LIMIT ?";
        List<Film> films = jdbcTemplate.query(SELECT_FILMS + sql, (rs, rowNum) -> makeFilm(rs), count);
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
                "WHERE fg.film_id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("genre_id"), rs.getString("genre_name")), film.getId());
        film.getGenres().clear();
        film.getGenres().addAll(genres);
    }

    private void updateGenres(Set<Genre> genres, Long id) {
        jdbcTemplate.update("DELETE FROM genre_film WHERE film_id = ?", id);
        if (genres != null && !genres.isEmpty()) {
            String sql = "INSERT INTO genre_film (film_id, genre_id) VALUES (?, ?)";
            Genre[] g = genres.toArray(new Genre[0]);
            jdbcTemplate.batchUpdate(
                    sql,
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setLong(1, id);
                            ps.setInt(2, g[i].getId());
                        }

                        @Override
                        public int getBatchSize() {
                            return g.length;
                        }
                    });
        }
    }
}
