package ru.yandex.practicum.filmorate;

import ru.yandex.practicum.filmorate.controller.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController controller;
    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService();
        controller = new UserController(service);
    }

    @Test
    void createUser_validUser() {
        User user = new User();
        user.setEmail("test@example.ru");
        user.setLogin("Login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("Name");

        User created = controller.createUser(user);

        assertNotNull(created.getId());
        assertEquals("test@example.ru", created.getEmail());
        assertEquals("Login", created.getLogin());
        assertEquals(LocalDate.of(2000, 1, 1), created.getBirthday());
        assertEquals("Name", created.getName());
    }

    @Test
    void createUser_emailNull_throwsValidationException() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createUser(user));
        assertTrue(ex.getMessage().contains("E-mail должен быть указан"));
    }

    @Test
    void createUser_emailNotValid() {
        User user = new User();
        user.setEmail("testexample.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createUser(user));
        assertTrue(ex.getMessage().contains("E-mail должен быть указан"));
    }

    @Test
    void createUser_loginWithSpace() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin("log in");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createUser(user));
        assertTrue(ex.getMessage().contains("Логин не может быть пустым и не должен содержать пробелы"));
    }

    @Test
    void createUser_birthdayInFuture() {
        User user = new User();
        user.setEmail("email@example.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2030,01,01));

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createUser(user));
        assertTrue(ex.getMessage().contains("Дата рождения не может быть в будущем"));
    }

    @Test
    void createUser_nameNull_setsNameToLogin() {
        User user = new User();
        user.setEmail("email@example.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName(null);

        User created = controller.createUser(user);

        assertEquals("login", created.getName());
    }

    @Test
    void createUser_duplicateEmail() {
        User user1 = new User();
        user1.setEmail("email@example.ru");
        user1.setLogin("login1");
        user1.setBirthday(LocalDate.of(2025, 1, 1));
        controller.createUser(user1);

        User user2 = new User();
        user2.setEmail("email@example.ru");
        user2.setLogin("login2");
        user2.setBirthday(LocalDate.of(2025, 1, 1));

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createUser(user2));
        assertTrue(ex.getMessage().contains("Этот E-mail уже используется"));
    }

    @Test
    void updateUser_validUpdate() {
        User user = new User();
        user.setEmail("email@example.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("Name");
        User created = controller.createUser(user);

        User update = new User();
        update.setId(created.getId());
        update.setEmail("newemail@example.ru");
        update.setLogin("newlogin");
        update.setBirthday(LocalDate.of(1999, 12, 31));
        update.setName("New Name");

        User updated = controller.updateUser(update);

        assertEquals("newemail@example.ru", updated.getEmail());
        assertEquals("newlogin", updated.getLogin());
        assertEquals(LocalDate.of(1999, 12, 31), updated.getBirthday());
        assertEquals("New Name", updated.getName());
    }

    @Test
    void updateUser_missingId() {
        User user = new User();
        user.setEmail("email@example.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.updateUser(user));
        assertTrue(ex.getMessage().contains("Id должен быть указан"));
    }

    @Test
    void updateUser_nonExistentId() {
        User user = new User();
        user.setId(2L);
        user.setEmail("email@example.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        NotFoundException ex = assertThrows(NotFoundException.class, () -> controller.updateUser(user));
        assertTrue(ex.getMessage().contains("не найден"));
    }

    @Test
    void updateUser_emailUsedByAnotherUser() {
        User user1 = new User();
        user1.setEmail("email1@example.ru");
        user1.setLogin("login1");
        user1.setBirthday(LocalDate.of(2000, 1, 1));
        User created1 = controller.createUser(user1);

        User user2 = new User();
        user2.setEmail("email2@example.ru");
        user2.setLogin("login2");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        User created2 = controller.createUser(user2);

        User update = new User();
        update.setId(created2.getId());
        update.setEmail("email1@example.ru");

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.updateUser(update));
        assertTrue(ex.getMessage().contains("Этот E-mail уже используется"));
    }

    @Test
    void updateUser_loginWithSpace() {
        User user = new User();
        user.setEmail("email@example.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        User created = controller.createUser(user);

        User update = new User();
        update.setId(created.getId());
        update.setLogin("invalid login");

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.updateUser(update));
        assertTrue(ex.getMessage().contains("Логин не может быть пустым"));
    }

    @Test
    void updateUser_birthdayInFuture() {
        User user = new User();
        user.setEmail("email@example.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        User created = controller.createUser(user);

        User update = new User();
        update.setId(created.getId());
        update.setBirthday(LocalDate.of(2030,01,01));

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.updateUser(update));
        assertTrue(ex.getMessage().contains("Дата рождения не может быть в будущем"));
    }

    @Test
    void updateUser_nameNull() {
        User user = new User();
        user.setEmail("email@example.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("Name");
        User created = controller.createUser(user);

        User update = new User();
        update.setId(created.getId());
        update.setName(null);

        User updated = controller.updateUser(update);
        assertEquals(updated.getLogin(), updated.getName());
    }
}

