package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import ru.yandex.practicum.filmorate.converters.*;
import java.time.Duration;
import java.time.LocalDate ;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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

    @JsonSerialize(using = DurationSecondsSerializer.class)
    @JsonDeserialize(using = DurationSecondsDeserializer.class)
    @Positive(message = "Продолжительность должна быть положительным числом")
    private Duration duration;
}
