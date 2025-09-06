package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import java.time.LocalDate;
import java.util.LinkedHashSet;

import jakarta.validation.constraints.*;

/**
 * Film.
 */
@Data
public class Film {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;

    @NotNull(message = "Дата релиза обязательна")
    private LocalDate releaseDate;

    private Integer duration;

    @NotNull
    private Mpa mpa;
    private final LinkedHashSet<Genre> genres = new LinkedHashSet<>();
}
