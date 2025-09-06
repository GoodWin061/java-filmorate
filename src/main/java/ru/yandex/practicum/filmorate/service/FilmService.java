package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final Map<Long, Set<Long>> filmLikes = new HashMap<>();

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("genreDbStorage") GenreStorage genreStorage,
                       @Qualifier("mpaDbStorage") MpaStorage mpaStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    public Collection<Film> findAll() {
        log.info("Запрос на получение всех фильмов.");
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        validateFilm(film, false);
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                findGenreById(genre.getId());
            }
        }

        if (film.getMpa() != null) {
            findMpaById(film.getMpa().getId());
        }
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            log.warn("Попытка обновления фильма без указания ID.");
            throw new ValidationException("Id должен быть указан.");
        }
        validateFilm(film, true);
        return filmStorage.update(film);
    }

    private void validateFilm(Film film, boolean isUpdate) {
        // Проверяем название
        if (!isUpdate || film.getName() != null) {
            if (film.getName() == null || film.getName().isBlank()) {
                log.warn("{} фильма с ID: {} пустое название",
                        isUpdate ? "Обновление" : "Создание", film.getId());
                throw new ValidationException("Название не может быть пустым");
            }
        }

        if (!isUpdate || film.getDescription() != null) {
            if (film.getDescription() == null || film.getDescription().isBlank()) {
                log.warn("{} фильма с ID: {} пустое описание",
                        isUpdate ? "Обновление" : "Создание", film.getId());
                throw new ValidationException("Описание не может быть пустым");
            }
            if (film.getDescription().length() > 200) {
                log.warn("Некорректное описание фильма при {} с ID: {}",
                        isUpdate ? "обновлении" : "создании", film.getId());
                throw new ValidationException("Описание фильма не может быть более 200 символов");
            }
        }

        LocalDate earliestDate = LocalDate.of(1895, 12, 28);
        if (!isUpdate || film.getReleaseDate() != null) {
            if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(earliestDate)) {
                log.warn("Некорректная дата релиза фильма при {} с ID: {}: {}",
                        isUpdate ? "обновлении" : "создании", film.getId(), film.getReleaseDate());
                throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
            }
        }

        if (!isUpdate || film.getDuration() != null) {
            if (film.getDuration() == null || film.getDuration() <= 0) {
                log.warn("Некорректная продолжительность фильма при {} с ID: {}: {}",
                        isUpdate ? "обновлении" : "создании", film.getId(), film.getDuration());
                throw new ValidationException("Продолжительность фильма должна быть положительным числом");
            }
        }
    }

    public void addLike(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId);
        if (film == null) {
            log.warn("Фильм с ID: {} не найден", filmId);
            throw new NotFoundException("Фильм не найден");
        }

        if (userStorage.getById(userId) == null) {
            log.warn("Пользователь с ID: {} не найден", userId);
            throw new NotFoundException("Пользователь не найден");
        }

        filmLikes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        log.info("Пользователь с ID: {} поставил лайк фильму с ID: {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        Set<Long> likes = filmLikes.get(filmId);
        if (likes == null || !likes.remove(userId)) {
            log.warn("Лайк пользователя с ID: {} не найден для фильма с ID: {}", userId, filmId);
            throw new NotFoundException("Лайк не найден");
        }
        log.info("Пользователь с ID: {} убрал лайк у фильма с ID: {}", userId, filmId);
    }

    public List<Film> getTopFilms(int count) {
        return filmLikes.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .limit(count)
                .map(Map.Entry::getKey)
                .map(filmStorage::findById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Film getById(Long id) {
        return filmStorage.findById(id);
    }

    public List<Genre> findAllGenres() {
        return genreStorage.findAllGenres();
    }

    public Genre findGenreById(int id) {
        return genreStorage.findGenreById(id).orElseThrow(() -> new GenreNotFoundException("Жанр не найден."));
    }

    public List<Mpa> findAllMpa() {
        return mpaStorage.findAllMpa();
    }

    public Mpa findMpaById(int id) {
        return mpaStorage.findMpaById(id).orElseThrow(() -> new MpaNotFoundException("Рейтинг MPA не найден."));
    }
}