package org.nmcpye.datarun.party.repository;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;

import org.nmcpye.datarun.party.entities.AssignmentMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * @author Hamza Assada 29/12/2025
 */
@Repository
public interface AssignmentMemberRepository extends BaseJpaRepository<AssignmentMember, Long> {

  @Query(value = """
      SELECT DISTINCT am.assignment_id
      FROM assignment_member am
      """, countQuery = """
      SELECT DISTINCT am.assignment_id
              FROM assignment_member am
      """, nativeQuery = true)
  Page<AssignmentMember> findAllAssignmentIds(Pageable pageable);

  List<AssignmentMember> findByAssignmentId(String assignmentId);

  /**
   * Return distinct active assignment ids for the given principals.
   * <p>
   * Principal format expected: "<TYPE>:<ID>" (e.g. "TEAM:abc123", "USER:u456").
   * Active = (valid_from IS NULL OR valid_from <= now())
   * AND (valid_to IS NULL OR valid_to > now())
   */
  @Query(value = """
      SELECT DISTINCT am
      FROM assignment_member am
      WHERE  am.member_id IN (:principalIds) AND am.assignment_id IN (:assignmentIds)
        AND (am.valid_from IS NULL OR am.valid_from <= now())
        AND (am.valid_to   IS NULL OR am.valid_to   > now())
      """, nativeQuery = true)
  List<AssignmentMember> findActiveAssignmentIdsForPrincipals(@Param("assignmentIds") Collection<String> assignmentIds,
      @Param("principalIds") Collection<String> principalIds);

  /**
   * Return distinct active assignment ids for the given principals.
   * <p>
   * Principal format expected: "<TYPE>:<ID>" (e.g. "TEAM:abc123", "USER:u456").
   * Active = (valid_from IS NULL OR valid_from <= now())
   * AND (valid_to IS NULL OR valid_to > now())
   */
  @Query(value = """
      SELECT DISTINCT a.id
      FROM assignment a
      LEFT JOIN team t ON t.id = a.team_id
      LEFT JOIN activity act ON act.id = a.activity_id
      WHERE (
        -- 1) assignment has at least one matching member (explicit members take precedence)
        EXISTS (
          SELECT 1 FROM assignment_member am
          WHERE am.assignment_id = a.id
            AND am.member_id IN (:principalIds)
            -- optional per-member validity; keep or remove as desired
            AND (am.valid_from IS NULL OR am.valid_from <= now())
            AND (am.valid_to IS NULL OR am.valid_to > now())
        )
      )
      OR (
        -- 2) no explicit members defined -> fallback to assignment.team_id
        NOT EXISTS (SELECT 1 FROM assignment_member am2 WHERE am2.assignment_id = a.id)
        AND a.team_id IN (:teamIds)
        -- team/activity enabled checks
        AND (t.disabled IS FALSE OR t.disabled IS NULL)
        AND (act.disabled IS FALSE OR act.disabled IS NULL)
        -- activity validity window (optional if you add these fields)
        AND (act.valid_from IS NULL OR act.valid_from <= now())
        AND (act.valid_to IS NULL OR act.valid_to > now())
      )
      """, nativeQuery = true)
  List<String> findActiveAssignmentIdsForPrincipalsAndTeams(
      @Param("principalIds") Collection<String> principalIds,
      @Param("teamIds") Collection<String> teamIds);

  @Query(value = """
      SELECT DISTINCT a.id
      FROM assignment a
      LEFT JOIN team t ON t.id = a.team_id
      LEFT JOIN activity act ON act.id = a.activity_id
      WHERE (
        EXISTS (
          SELECT 1 FROM assignment_member am
          WHERE am.assignment_id = a.id
            AND am.member_id IN (:principalIds)
            AND (am.valid_from IS NULL OR am.valid_from <= now())
            AND (am.valid_to IS NULL OR am.valid_to > now())
        )
      )
      OR (
        NOT EXISTS (SELECT 1 FROM assignment_member am2 WHERE am2.assignment_id = a.id)
        AND a.team_id IN (:teamIds)
        AND (t.disabled IS FALSE OR t.disabled IS NULL)
        AND (act.disabled IS FALSE OR act.disabled IS NULL)
        AND (act.valid_from IS NULL OR act.valid_from <= now())
        AND (act.valid_to IS NULL OR act.valid_to > now())
      )
      """, countQuery = """
      SELECT COUNT(DISTINCT a.id)
      FROM assignment a
      LEFT JOIN team t ON t.id = a.team_id
      LEFT JOIN activity act ON act.id = a.activity_id
      WHERE (
        EXISTS (
          SELECT 1 FROM assignment_member am
          WHERE am.assignment_id = a.id
            AND am.member_id IN (:principalIds)
            AND (am.valid_from IS NULL OR am.valid_from <= now())
            AND (am.valid_to IS NULL OR am.valid_to > now())
        )
      )
      OR (
        NOT EXISTS (SELECT 1 FROM assignment_member am2 WHERE am2.assignment_id = a.id)
        AND a.team_id IN (:teamIds)
        AND (t.disabled IS FALSE OR t.disabled IS NULL)
        AND (act.disabled IS FALSE OR act.disabled IS NULL)
        AND (act.valid_from IS NULL OR act.valid_from <= now())
        AND (act.valid_to IS NULL OR act.valid_to > now())
      )
      ORDER BY
      a.last_modified_date
      """, nativeQuery = true)
  Page<String> findActiveAssignmentIdsForPrincipalsAndTeams(
      @Param("principalIds") Collection<String> principalIds,
      @Param("teamIds") Collection<String> teamIds,
      Pageable pageable);

  @Query(value = """
      SELECT DISTINCT a.id
      FROM assignment a
      LEFT JOIN team t ON t.id = a.team_id
      LEFT JOIN activity act ON act.id = a.activity_id
      WHERE (
        EXISTS (
          SELECT 1 FROM assignment_member am
          WHERE am.assignment_id = a.id
            AND am.member_id IN (:principalIds)
            AND (am.valid_from IS NULL OR am.valid_from <= now())
            AND (am.valid_to IS NULL OR am.valid_to > now())
            AND am.last_modified_date > :since
        )
      )
      OR (
        NOT EXISTS (SELECT 1 FROM assignment_member am2 WHERE am2.assignment_id = a.id)
        AND a.team_id IN (:teamIds)
        AND (t.disabled IS FALSE OR t.disabled IS NULL)
        AND (act.disabled IS FALSE OR act.disabled IS NULL)
        AND (act.valid_from IS NULL OR act.valid_from <= now())
        AND (act.valid_to IS NULL OR act.valid_to > now())
        AND a.last_modified_date > :since
      )
      """, countQuery = """
      SELECT COUNT(DISTINCT a.id)
      FROM assignment a
      LEFT JOIN team t ON t.id = a.team_id
      LEFT JOIN activity act ON act.id = a.activity_id
      WHERE (
        EXISTS (
          SELECT 1 FROM assignment_member am
          WHERE am.assignment_id = a.id
            AND am.member_id IN (:principalIds)
            AND (am.valid_from IS NULL OR am.valid_from <= now())
            AND (am.valid_to IS NULL OR am.valid_to > now())
            AND am.last_modified_date > :since
        )
      )
      OR (
        NOT EXISTS (SELECT 1 FROM assignment_member am2 WHERE am2.assignment_id = a.id)
        AND a.team_id IN (:teamIds)
        AND (t.disabled IS FALSE OR t.disabled IS NULL)
        AND (act.disabled IS FALSE OR act.disabled IS NULL)
        AND (act.valid_from IS NULL OR act.valid_from <= now())
        AND (act.valid_to IS NULL OR act.valid_to > now())
        AND a.last_modified_date > :since
      )
      ORDER BY
      a.last_modified_date
      """, nativeQuery = true)
  Page<String> findActiveAssignmentIdsForPrincipalsAndTeams(
      @Param("principalIds") Collection<String> principalIds,
      @Param("teamIds") Collection<String> teamIds,
      @Param("since") Instant since,
      Pageable pageable);
}
