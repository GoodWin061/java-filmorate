package ru.yandex.practicum.filmorate;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class FilmorateApplication {
	public static void main(String[] args) {
		SpringApplication.run(FilmorateApplication.class, args);
		log.info("Программа оценки фильмов запущена");
	}

}
