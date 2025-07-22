package ru.yandex.practicum.filmorate;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Scanner;

@Slf4j
@SpringBootApplication
public class FilmorateApplication {
	public static void main(String[] args) {
		SpringApplication.run(FilmorateApplication.class, args);
		log.info("Программа оценки фильмов запущена");
		final Gson gson = new Gson();
		final Scanner scanner = new Scanner(System.in);
		System.out.print("Введите JSON => ");
		log.trace("Ждем запрос JSON");
		final String input = scanner.nextLine();
		try {
			gson.fromJson(input, Map.class);
			System.out.println("Был введён корректный JSON");
		} catch (JsonSyntaxException exception) {
			System.out.println("Был введён некорректный JSON");
		}
	}

}
