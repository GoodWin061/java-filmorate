# java-filmorate
Template repository for Filmorate project.
## Диаграмма базы данных
[Ссылка на диаграмму базы данных](https://dbdiagram.io/d/68a96aad1e7a6119673df7ae)

🎬 База данных для киносервиса
📊 Структура базы данных
Основные сущности
👤 Пользователи (user)
Поле	Тип	Описание
user_id	INTEGER (PK, AUTO_INCREMENT)	Уникальный идентификатор пользователя
email	VARCHAR (NOT NULL, UNIQUE)	Электронная почта пользователя
login	VARCHAR (NOT NULL)	Логин пользователя
name	VARCHAR	Имя пользователя (может быть пустым)
birthday	DATE	Дата рождения пользователя
🎬 Фильмы (film)
Поле	Тип	Описание
film_id	INTEGER (PK, AUTO_INCREMENT)	Уникальный идентификатор фильма
name	VARCHAR (NOT NULL)	Название фильма
description	VARCHAR(200)	Описание фильма (макс. 200 символов)
release_date	DATE (NOT NULL)	Дата выхода фильма
duration	INTERVAL	Продолжительность фильма
mpa_id	INTEGER (FK)	Ссылка на возрастной рейтинг
👥 Дружба (friendships)
Поле	Тип	Описание
user_id	INTEGER (FK → user.user_id)	Идентификатор пользователя
friend_id	INTEGER (FK → user.user_id)	Идентификатор друга
friend_request	BOOLEAN	Статус подтверждения дружбы
❤️ Лайки (like)
Поле	Тип	Описание
film_id	INTEGER (FK → film.film_id)	Идентификатор фильма
user_id	INTEGER (FK → user.user_id)	Идентификатор пользователя
🎭 Жанры (genre)
Поле	Тип	Описание
genre_id	INTEGER (PK)	Уникальный идентификатор жанра
name	VARCHAR	Название жанра
🔗 Связь жанров и фильмов (genre_film)
Поле	Тип	Описание
genre_id	INTEGER (PK, FK → genre.genre_id)	Идентификатор жанра
film_id	INTEGER (PK, FK → film.film_id)	Идентификатор фильма
📊 Возрастные рейтинги (mpa_ratings)
Поле	Тип	Описание
mpa_id	INTEGER (PK)	Уникальный идентификатор рейтинга
name	VARCHAR (NOT NULL)	Название рейтинга
description	VARCHAR	Описание рейтинга
    name - название рейтинга (обязательное поле)
    description - описание рейтинга


