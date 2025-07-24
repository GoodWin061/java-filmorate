package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class FilmService {

    private final Map<Long, Film> films = new HashMap<>();

    public Collection<Film> findAll() {
        return films.values();
    }

    public Film create(Film film) {
        validateFilm(film, false);

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            log.warn("Попытка обновления фильма без указания ID");
            throw new ValidationException("Id должен быть указан");
        }

        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }

        validateFilm(film, true);

        Film oldFilm = films.get(film.getId());
        oldFilm.setName(film.getName());
        oldFilm.setDescription(film.getDescription());
        oldFilm.setReleaseDate(film.getReleaseDate());
        oldFilm.setDuration(film.getDuration());

        log.info("Фильм с ID {} успешно обновлён", film.getId());
        return oldFilm;
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

    private long getNextId() {
        return films.keySet()
                .stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0) + 1;
    }
}