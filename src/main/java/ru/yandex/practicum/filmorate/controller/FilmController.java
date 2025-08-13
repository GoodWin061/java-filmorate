package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Вывод всех фильмов");
        return filmService.findAll();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.debug("Начало создания фильма с названием: {}", film.getName());
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        log.debug("Начало обновления фильма с ID: {}", film.getId());
        return filmService.update(film);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.info("Запрос на получение фильма с ID: {}", id);
        return filmService.getById(id);
    }

    // Лайк фильма
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь с ID {} ставит лайк фильму с ID {}", userId, id);
        filmService.addLike(id, userId);
    }

    // Удаление лайка
    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь с ID {} удаляет лайк у фильма с ID {}", userId, id);
        filmService.removeLike(id, userId);
    }

    // Получение популярных фильмов
    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(@RequestParam(defaultValue = "10", required = false) Integer count) {
        log.info("Запрос на получение первых {} популярных фильмов", count);
        return filmService.getTopFilms(count);
    }
}