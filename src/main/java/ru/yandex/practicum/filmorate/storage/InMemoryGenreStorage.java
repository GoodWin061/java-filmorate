package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

public class InMemoryGenreStorage implements GenreStorage {
    private final Map<Integer, Genre> genres = new HashMap<>();

    public InMemoryGenreStorage() {
        genres.put(1, new Genre(1, "Комедия"));
        genres.put(2, new Genre(2, "Драма"));
        genres.put(3, new Genre(3, "Триллер"));
        genres.put(4, new Genre(4, "Ужасы"));
        genres.put(5, new Genre(5, "Фантастика"));
    }

    @Override
    public List<Genre> findAllGenres() {
        return new ArrayList<>(genres.values());
    }

    @Override
    public Optional<Genre> findGenreById(int id) {
        return Optional.ofNullable(genres.get(id));
    }

    @Override
    public void findAllGenresByFilm(List<Film> films) {
    }
}
