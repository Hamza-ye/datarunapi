package org.nmcpye.datarun.analytics.etl.repository;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.analytics.etl.dto.EventDto;
import org.nmcpye.datarun.analytics.etl.dto.EventRow;
import org.nmcpye.datarun.analytics.etl.model.SubmissionContext;
import org.nmcpye.datarun.analytics.util.UuidUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("ConcatenationWithEmptyString")
@Repository
@RequiredArgsConstructor
public class EventEntityJdbcRepository {
    private final NamedParameterJdbcTemplate jdbc;
    final static String sql = ""
        + "INSERT INTO analytics.events ("
        + "event_id, event_type, submission_uid, submission_id, submission_serial, "
        + "parent_event_id, event_ce_id, assignment_uid, activity_uid, org_unit_uid, team_uid, template_uid, "
        + "submission_creation_time, start_time, created_by, last_modified_by, "
        + "created_at, updated_at, deleted_at) "
        + "VALUES ("
        + ":eventId, :eventType, :submissionUid, :submissionId, :submissionSerial, "
        + ":parentEventId, :eventCeId, :assignmentUid, :activityUid, :orgUnitUid, :teamUid, :templateUid, "
        + ":submissionCreationTime, :startTime, :createdBy, :lastModifiedBy, "
        + "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :deletedAt) "
        + "ON CONFLICT (event_id) DO UPDATE SET "
        + "deleted_at = EXCLUDED.deleted_at, "
        + "event_type = COALESCE(EXCLUDED.event_type, events.event_type), "
        + "submission_uid = COALESCE(EXCLUDED.submission_uid, events.submission_uid), "
        + "submission_id = COALESCE(EXCLUDED.submission_id, events.submission_id), "
        + "submission_serial = COALESCE(EXCLUDED.submission_serial, events.submission_serial), "
        + "parent_event_id = COALESCE(EXCLUDED.parent_event_id, events.parent_event_id), "
        + "event_ce_id = COALESCE(EXCLUDED.event_ce_id, events.event_ce_id), "
        + "assignment_uid = COALESCE(EXCLUDED.assignment_uid, events.assignment_uid), "
        + "activity_uid = COALESCE(EXCLUDED.activity_uid, events.activity_uid), "
        + "org_unit_uid = COALESCE(EXCLUDED.org_unit_uid, events.org_unit_uid), "
        + "team_uid = COALESCE(EXCLUDED.team_uid, events.team_uid), "
        + "template_uid = COALESCE(EXCLUDED.template_uid, events.template_uid), "
        + "created_by = COALESCE(events.created_by, EXCLUDED.created_by), "
        + "last_modified_by = COALESCE(events.last_modified_by, EXCLUDED.last_modified_by), "
        + "submission_creation_time = COALESCE(events.submission_creation_time, EXCLUDED.submission_creation_time), "
        + "start_time = COALESCE(events.start_time, EXCLUDED.start_time), "
        + "updated_at = CURRENT_TIMESTAMP";

    public void upsertEventEntity(EventDto eventDto) {
        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("eventId", eventDto.eventId())
            .addValue("eventType", eventDto.eventType())
            .addValue("submissionUid", eventDto.submissionUid())
            .addValue("submissionId", eventDto.submissionId())
            .addValue("assignmentUid", eventDto.assignmentUid())
            .addValue("submissionSerial", eventDto.submissionSerial())
            .addValue("parentEventId", eventDto.parentEventId())
            .addValue("eventCeId", eventDto.eventCeId())
            .addValue("activityUid", eventDto.activityUid())
            .addValue("orgUnitUid", eventDto.orgUnitUid())
            .addValue("teamUid", eventDto.teamUid())
            .addValue("templateUid", eventDto.templateUid())
            .addValue("submissionCreationTime", getTimestamp(eventDto.submissionCreationTime()))
            .addValue("startTime", getTimestamp(eventDto.startTime()))
            .addValue("createdBy", eventDto.createdBy())
            .addValue("lastModifiedBy", eventDto.lastModifiedBy())
            .addValue("deletedAt", getTimestamp(eventDto.deletedAt()));

        jdbc.update(sql, p);
    }

    public Optional<EventDto> findByInstanceKey(String eventId) {
        if (eventId == null) return Optional.empty();
        String sql = "SELECT * FROM analytics.events WHERE event_id = :eventId LIMIT 1";
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("eventId", eventId);
        List<EventDto> rows = jdbc.query(sql, p, EventDto.ROW_MAPPER);
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(rows.get(0));
    }

    public void patchUpsertEvents(SubmissionContext context, List<EventRow> events) {
        if (events == null || events.isEmpty()) return;
        // prepare batch params
        MapSqlParameterSource[] batch = new MapSqlParameterSource[events.size()];
        int i = 0;
        for (EventRow e : events) {
            MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("eventId", e.eventId())
                .addValue("eventType", e.eventType())
                .addValue("submissionUid", context.submissionUid())
                .addValue("submissionId", context.submissionId())
                .addValue("assignmentUid", context.assignmentUid())
                .addValue("submissionSerial", context.submissionSerial())
                .addValue("parentEventId", e.parentEventId())
                .addValue("eventCeId", UuidUtils.toUuidOrNull(e.eventCeId()))
                .addValue("activityUid", context.activityUid())
                .addValue("orgUnitUid", context.orgUnitUid())
                .addValue("teamUid", context.teamUid())
                .addValue("templateUid", context.templateUid())
                .addValue("createdBy", context.createdBy())
                .addValue("lastModifiedBy", context.lastModifiedBy())
                .addValue("submissionCreationTime", getTimestamp(context.submissionCreationTime())) // creating time at server
                .addValue("startTime", getTimestamp(context.startTime())) // creating time at client
                .addValue("deletedAt", getTimestamp(context.deletedAt()));
            batch[i++] = p;
        }

        jdbc.batchUpdate(sql, batch);
    }

    public void markAllAsDeletedForSubmission(String submissionUid) {
        String sql = "UPDATE analytics.events SET deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP " +
            "WHERE submission_uid = :submissionUid AND deleted_at IS NULL";
        jdbc.update(sql, new MapSqlParameterSource("submissionUid", submissionUid));
    }

    Timestamp getTimestamp(Instant instant) {
        if (instant == null) return null;
        return Timestamp.from(instant);
    }
}
