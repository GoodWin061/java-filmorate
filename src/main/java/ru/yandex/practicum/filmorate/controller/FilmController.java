package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Вывод всех фильмов");
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        // проверяем выполнение необходимых условий
        // проверка названия
        log.debug("Начало создания фильма с названием: {}", film.getName());
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Попытка создать фильм без названия");
            throw new ValidationException("Название не может быть пустым");
        }

        // проверка длины описания
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Некорректное описание фильма длиною более 200 символов");
            throw new ValidationException("Описание фильма не может быть более 200 символов");
        }

        // Проверка даты релиза (не раньше 28 декабря 1895)
        LocalDate earliestReleaseDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(earliestReleaseDate)) {
            log.warn("Некорректная дата релиза фильма: '{}'", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        // Проверка продолжительности (должна быть положительной)
        if (film.getDuration() == null || film.getDuration().toSeconds() <= 0) {
            log.warn("Задан 0 или отрицательная продолжительность фильма: '{}'", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        // формируем дополнительные данные
        film.setId(getNextId());
        // сохраняем новую публикацию в памяти приложения
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.debug("Начало обновления фильма с ID: {}", newFilm.getId());

        if (newFilm.getId() == null) {
            log.warn("Попытка обновления фильма без указания ID");
            throw new ValidationException("Id должен быть указан");
        }

        if (!films.containsKey(newFilm.getId())) {
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }

        // Проверяем название (обязательное, не пустое)
        if (newFilm.getName() == null || newFilm.getName().isBlank()) {
            log.warn("Задано пустое название фильма при обновлении с ID: {}", newFilm.getId());
            throw new ValidationException("Название не может быть пустым");
        }

        // Проверяем описание (обязательное, не пустое, максимум 200 символов)
        if (newFilm.getDescription() == null || newFilm.getDescription().isBlank()) {
            log.warn("Задано пустое описание фильма при обновлении с ID: {}", newFilm.getId());
            throw new ValidationException("Описание не может быть пустым");
        }
        if (newFilm.getDescription().length() > 200) {
            log.warn("Некорректное описание фильма при обновлении с ID: {}", newFilm.getId());
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        // Проверяем дату релиза
        LocalDate earliestDate = LocalDate.of(1895, 12, 28);
        if (newFilm.getReleaseDate() != null && newFilm.getReleaseDate().isBefore(earliestDate)) {
            log.warn("Попытка обновить фильм с ID: {} указана некорректная дата релиза {}", newFilm.getId(), newFilm.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        // Проверяем продолжительность
        if (newFilm.getDuration() != null && newFilm.getDuration().toSeconds() <= 0) {
            log.warn("Попытка обновить фильм с ID: {} указан 0 или отрицательная продолжительность {}", newFilm.getId(), newFilm.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        // Получаем старый фильм и обновляем поля
        Film oldFilm = films.get(newFilm.getId());
        oldFilm.setName(newFilm.getName());
        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());
        oldFilm.setDuration(newFilm.getDuration());

        log.info("Фильм с ID {} успешно обновлён", newFilm.getId());
        return oldFilm;
    }
}