package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.function.UnaryOperator.identity;

@RequiredArgsConstructor
@Repository
public class GenreDbStorage implements GenreStorage {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<Genre> findAllGenres() {
        String sql = "SELECT * FROM genre";
        return namedParameterJdbcTemplate.query(sql, (rs, rowNum) -> makeGenre(rs));
    }

    @Override
    public Optional<Genre> findGenreById(int id) {
        String sql = "SELECT * FROM genre WHERE genre_id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        List<Genre> genres = namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> makeGenre(rs));
        return genres.stream().findFirst();
    }

    public void findAllGenresByFilm(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        final Map<Long, Film> filmById = films.stream()
                .collect(Collectors.toMap(Film::getId, identity()));

        String sql = "SELECT gf.film_id, g.genre_id, g.name " +
                "FROM genre_film gf " +
                "JOIN genre g ON gf.genre_id = g.genre_id " +
                "WHERE gf.film_id IN (:filmIds)";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("filmIds", filmById.keySet());

        namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Film film = filmById.get(rs.getLong("film_id"));
            if (film != null) {
                film.getGenres().add(makeGenre(rs));
            }
            return null;
        });
    }

    private Genre makeGenre(ResultSet rs) throws SQLException {
        int id = rs.getInt("genre_id");
        String name = rs.getString("name");
        return new Genre(id, name);
    }
}
