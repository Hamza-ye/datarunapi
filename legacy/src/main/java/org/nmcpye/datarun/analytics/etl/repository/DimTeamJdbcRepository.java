package org.nmcpye.datarun.analytics.etl.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DimTeamJdbcRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public Optional<Map<String,Object>> findByUidOrId(String id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT * FROM public.team WHERE uid = :id OR id = :id LIMIT 1";
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("id", id);
        var rows = jdbc.queryForList(sql, p);
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(rows.get(0));
    }


    public Optional<Map<String,Object>> findByActivityAndCodeOrUid(String activityUid, String token) {
        if (activityUid == null || token == null) return Optional.empty();
        String sql = "SELECT * FROM public.team t "
            + "JOIN public.activity act ON act.id = t.activity_id "
            + "WHERE act.uid = :activityUid AND (t.code = :t OR t.uid = :t OR t.id = :t) "
            + "ORDER BY CASE WHEN t.code = :t THEN 1 WHEN t.uid = :t THEN 2 ELSE 3 END "
            + "LIMIT 1";
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("activityUid", activityUid)
            .addValue("t", token);
        var rows = jdbc.queryForList(sql, p);
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(rows.get(0));
    }
}

