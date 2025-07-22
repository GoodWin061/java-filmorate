package ru.yandex.practicum.filmorate;

import ru.yandex.practicum.filmorate.controller.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController controller;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
    }

    @Test
    void createFilm_withEarliestReleaseDate() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(Duration.ofMinutes(90));

        Film created = controller.create(film);
        assertNotNull(created.getId());
        assertEquals(LocalDate.of(1895, 12, 28), created.getReleaseDate());
    }

    @Test
    void createFilm_withReleaseDateBeforeEarliest() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(Duration.ofMinutes(90));

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(film));
        assertTrue(ex.getMessage().contains("Дата релиза не может быть раньше 28 декабря 1895 года"));
    }

    @Test
    void createFilm_withDescription200Chars() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание оп");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(Duration.ofMinutes(90));

        Film created = controller.create(film);
        assertEquals(200, created.getDescription().length());
    }

    @Test
    void createFilm_withDescription201Chars() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание опи");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(Duration.ofMinutes(90));

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(film));
        assertTrue(ex.getMessage().contains("Описание фильма не может быть более 200 символов"));
    }

    @Test
    void createFilm_withZeroDuration() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(Duration.ZERO);

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(film));
        assertTrue(ex.getMessage().contains("Продолжительность фильма должна быть положительным числом"));
    }

    @Test
    void createFilm_withNegativeDuration() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(Duration.ofSeconds(-10));

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(film));
        assertTrue(ex.getMessage().contains("Продолжительность фильма должна быть положительным числом"));
    }

    @Test
    void updateFilm_withValidData() {
        // Сначала создаём фильм
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(Duration.ofMinutes(100));
        Film created = controller.create(film);

        // Обновляем фильм с граничными значениями
        Film update = new Film();
        update.setId(created.getId());
        update.setName("Обновлённый фильм");
        update.setDescription("a".repeat(200));
        update.setReleaseDate(LocalDate.of(1895, 12, 28));
        update.setDuration(Duration.ofSeconds(1));

        Film updated = controller.update(update);
        assertEquals("Обновлённый фильм", updated.getName());
        assertEquals(200, updated.getDescription().length());
        assertEquals(LocalDate.of(1895, 12, 28), updated.getReleaseDate());
        assertEquals(Duration.ofSeconds(1), updated.getDuration());
    }

    @Test
    void updateFilm_withInvalidReleaseDate() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(Duration.ofMinutes(100));
        Film created = controller.create(film);

        Film update = new Film();
        update.setId(created.getId());
        update.setName("Обновлённый фильм");
        update.setDescription("Описание");
        update.setReleaseDate(LocalDate.of(1895, 12, 27));
        update.setDuration(Duration.ofMinutes(100));

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.update(update));
        assertTrue(ex.getMessage().contains("Дата релиза не может быть раньше 28 декабря 1895 года"));
    }

    @Test
    void updateFilm_withTooLongDescription() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(Duration.ofMinutes(100));
        Film created = controller.create(film);

        Film update = new Film();
        update.setId(created.getId());
        update.setName("Обновлённый фильм");
        update.setDescription("Описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание описание опи");
        update.setReleaseDate(LocalDate.of(2025, 1, 1));
        update.setDuration(Duration.ofMinutes(100));

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.update(update));
        assertTrue(ex.getMessage().contains("Максимальная длина описания — 200 символов"));
    }

    @Test
    void updateFilm_withZeroDuration() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(Duration.ofMinutes(100));
        Film created = controller.create(film);

        Film update = new Film();
        update.setId(created.getId());
        update.setName("Обновлённый фильм");
        update.setDescription("Описание");
        update.setReleaseDate(LocalDate.of(2025, 1, 1));
        update.setDuration(Duration.ZERO);

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.update(update));
        assertTrue(ex.getMessage().contains("Продолжительность фильма должна быть положительным числом"));
    }
}

