package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class MpaDbStorage implements MpaStorage {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<Mpa> findAllMpa() {
        String sql = "SELECT * FROM mpa_rating";
        return namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource(), (rs, rowNum) -> makeMpa(rs));
    }

    @Override
    public Optional<Mpa> findMpaById(int id) {
        String sql = "SELECT * FROM mpa_rating WHERE mpa_id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        List<Mpa> mpas = namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> makeMpa(rs));
        return mpas.stream().findFirst();
    }

    private Mpa makeMpa(ResultSet rs) throws SQLException {
        int id = rs.getInt("mpa_id");
        String name = rs.getString("name");
        return new Mpa(id, name);
    }
}
