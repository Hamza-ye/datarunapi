package org.nmcpye.datarun.analytics.etl.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DimOrgUnitJdbcRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public Optional<Map<String,Object>> findByUid(String id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT * FROM public.org_unit WHERE id = :uid OR id = :id LIMIT 1";
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("id", id);
        var rows = jdbc.queryForList(sql, p);
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(rows.get(0));
    }

    public Optional<Map<String,Object>> findByCodeOrUidOrId(String token) {
        if (token == null) return Optional.empty();
        String sql = "SELECT * FROM public.org_unit "
            + "WHERE uid = :t OR code = :t OR id = :t "
            + "ORDER BY CASE WHEN uid = :t THEN 1 WHEN code = :t THEN 2 ELSE 3 END LIMIT 1";
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("t", token);
        var rows = jdbc.queryForList(sql, p);
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(rows.get(0));
    }
}

