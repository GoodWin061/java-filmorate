package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final Map<Long, Set<Long>> filmLikes = new HashMap<>();

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> findAll() {
        log.info("Запрос на получение всех фильмов.");
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        validateFilm(film, false);
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

        // Проверяем описание
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

        // Проверяем дату релиза
        LocalDate earliestDate = LocalDate.of(1895, 12, 28);
        if (!isUpdate || film.getReleaseDate() != null) {
            if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(earliestDate)) {
                log.warn("Некорректная дата релиза фильма при {} с ID: {}: {}",
                        isUpdate ? "обновлении" : "создании", film.getId(), film.getReleaseDate());
                throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
            }
        }

        // Проверяем продолжительность
        if (!isUpdate || film.getDuration() != null) {
            if (film.getDuration() == null || film.getDuration().toSeconds() <= 0) {
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

        // Проверяем, существует ли пользователь с указанным ID
        if (userStorage.getById(userId) == null) {
            log.warn("Пользователь с ID: {} не найден", userId);
            throw new NotFoundException("Пользователь не найден");
        }

        // Добавляем лайк, если его еще нет
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
        // Получаем 10 наиболее популярных фильмов по количеству лайков
        return filmLikes.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size())) // Сортируем по количеству лайков
                .limit(count) // Ограничиваем количество
                .map(Map.Entry::getKey) // Получаем ID фильмов
                .map(filmStorage::findById) // Получаем фильмы по ID
                .filter(Objects::nonNull) // Фильтруем null
                .collect(Collectors.toList()); // Собираем в список
    }

    public Film getById(Long id) {
        return filmStorage.findById(id);
    }
}