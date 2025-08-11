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
}