# java-filmorate
Template repository for Filmorate project.
## Диаграмма базы данных
[Ссылка на диаграмму базы данных](https://dbdiagram.io/d/68a96aad1e7a6119673df7ae)

# 🎬 База данных для киносервиса

## 📊 Структура базы данных

### Основные сущности

#### 👤 Пользователи (`users`)
| Поле | Тип | Описание |
|------|-----|----------|
| `user_id` | INTEGER (PK, AUTO_INCREMENT) | Уникальный идентификатор пользователя |
| `email` | VARCHAR (NOT NULL, UNIQUE) | Электронная почта пользователя |
| `login` | VARCHAR (NOT NULL) | Логин пользователя |
| `name` | VARCHAR | Имя пользователя (может быть пустым) |
| `birthday` | DATE | Дата рождения пользователя |

#### 🎬 Фильмы (`film`)
| Поле | Тип | Описание |
|------|-----|----------|
| `film_id` | INTEGER (PK, AUTO_INCREMENT) | Уникальный идентификатор фильма |
| `name` | VARCHAR (NOT NULL) | Название фильма |
| `description` | VARCHAR(200) | Описание фильма (макс. 200 символов) |
| `release_date` | DATE (NOT NULL) | Дата выхода фильма |
| `duration` | INTEGER | Продолжительность фильма |
| `mpa_id` | INTEGER (FK) | Ссылка на возрастной рейтинг |

#### 👥 Дружба (`friendships`)
| Поле | Тип | Описание |
|------|-----|----------|
| `user_id` | INTEGER (FK → user.user_id) | Идентификатор пользователя |
| `friend_id` | INTEGER (FK → user.user_id) | Идентификатор друга |
| `friend_request` | BOOLEAN | Статус подтверждения дружбы |

#### ❤️ Лайки (`likes`)
| Поле | Тип | Описание |
|------|-----|----------|
| `film_id` | INTEGER (FK → film.film_id) | Идентификатор фильма |
| `user_id` | INTEGER (FK → user.user_id) | Идентификатор пользователя |

#### 🎭 Жанры (`genre`)
| Поле | Тип | Описание |
|------|-----|----------|
| `genre_id` | INTEGER (PK) | Уникальный идентификатор жанра |
| `name` | VARCHAR | Название жанра |

#### 🔗 Связь жанров и фильмов (`genre_film`)
| Поле | Тип | Описание |
|------|-----|----------|
| `genre_id` | INTEGER (PK, FK → genre.genre_id) | Идентификатор жанра |
| `film_id` | INTEGER (PK, FK → film.film_id) | Идентификатор фильма |

#### 📊 Возрастные рейтинги (`mpa_rating`)
| Поле | Тип | Описание |
|------|-----|----------|
| `mpa_id` | INTEGER (PK) | Уникальный идентификатор рейтинга |
| `name` | VARCHAR (NOT NULL) | Название рейтинга |
| `description` | VARCHAR | Описание рейтинга |

## SQL Запросы
### 1. Получить всех пользователей с их email и датой рождения
```sql
SELECT user_id, email, login, name, birthday 
FROM user 
ORDER BY user_id;
  ```

### 2. Получить топ-10 самых популярных фильмов по количеству лайков
```sql
SELECT f.film_id, f.name, f.release_date, COUNT(l.user_id) as likes_count
FROM film f
LEFT JOIN like l ON f.film_id = l.film_id
GROUP BY f.film_id, f.name, f.release_date
ORDER BY likes_count DESC
LIMIT 10;
  ```

### 3. Получить фильмы с определенным возрастным рейтингом (PG-13)
```sql
SELECT f.film_id, f.name, f.release_date, f.duration, g.name as genre
FROM film f
JOIN mpa_rating m ON f.mpa_id = m.mpa_id
LEFT JOIN genre_film gf ON f.film_id = gf.film_id
LEFT JOIN genre g ON gf.genre_id = g.genre_id
WHERE m.name = 'PG-13'
ORDER BY f.release_date DESC;
  ```

### 4. Получить ТОП-10 самых активных пользователей (по количеству поставленных лайков)
```sql
SELECT u.user_id, u.login, u.name, COUNT(l.film_id) as likes_given
FROM user u
LEFT JOIN likes l ON u.user_id = l.user_id
GROUP BY u.user_id, u.login, u.name
ORDER BY likes_given DESC
LIMIT 10;
  ```

### 5. Получить фильмы, вышедшие в определенный год (2023)
```sql
SELECT f.film_id, f.name, f.description, f.release_date, m.name as mpa_rating
FROM film f
JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
WHERE EXTRACT(YEAR FROM f.release_date) = 2023
ORDER BY f.release_date DESC;
  ```
