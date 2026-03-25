package org.nmcpye.datarun.service.acl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;
import org.nmcpye.datarun.web.rest.v2.datasubmission.service.SubmissionNormalizer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SubmissionNormalizer}.
 * No Spring context needed — pure static functions.
 */
class SubmissionNormalizerTest {

  private ObjectMapper mapper;
  private List<SectionTemplateElementDto> sections;

  @BeforeEach
  void setUp() {
    mapper = new ObjectMapper();

    // Build sections matching the V1 sample
    SectionTemplateElementDto main = new SectionTemplateElementDto();
    main.setName("main");
    main.setRepeatable(false);
    main.setOrder(100);

    SectionTemplateElementDto patients = new SectionTemplateElementDto();
    patients.setName("patients");
    patients.setRepeatable(false);
    patients.setOrder(200);

    SectionTemplateElementDto medicines = new SectionTemplateElementDto();
    medicines.setName("medicines");
    medicines.setRepeatable(true);
    medicines.setOrder(300);

    SectionTemplateElementDto referrals = new SectionTemplateElementDto();
    referrals.setName("referrals");
    referrals.setRepeatable(false);
    referrals.setOrder(400);

    sections = List.of(main, patients, medicines, referrals);
  }

  @Test
  @DisplayName("T1: Simple form with no repeaters normalizes to flat values")
  void simpleFormNoRepeaters() {
    ObjectNode v1 = mapper.createObjectNode();
    ObjectNode mainSection = mapper.createObjectNode();
    mainSection.put("visitdate", "2025-09-27");
    mainSection.put("NotificationNumber", 2);
    v1.set("main", mainSection);

    ObjectNode patientsSection = mapper.createObjectNode();
    patientsSection.put("gender", "MALE");
    patientsSection.put("PatientName", "John");
    v1.set("patients", patientsSection);

    // Use only non-repeater sections
    List<SectionTemplateElementDto> nonRepeatSections = sections.stream()
        .filter(s -> !Boolean.TRUE.equals(s.getRepeatable()))
        .toList();

    JsonNode result = SubmissionNormalizer.normalize(v1, nonRepeatSections, mapper);

    assertNotNull(result.get("values"));
    assertNotNull(result.get("collections"));

    JsonNode values = result.get("values");
    assertEquals("2025-09-27", values.get("visitdate").asText());
    assertEquals(2, values.get("NotificationNumber").asInt());
    assertEquals("MALE", values.get("gender").asText());
    assertEquals("John", values.get("PatientName").asText());

    // Collections should be empty
    assertEquals(0, result.get("collections").size());
  }

  @Test
  @DisplayName("T2: Single repeater normalizes to identity-keyed collection map")
  void singleRepeater() throws Exception {
    String v1Json = """
        {
          "main": { "visitdate": "2025-09-27" },
          "medicines": [
            {
              "_id": "01K693VTPPWQR1M23AN06B6N0D",
              "_index": 1,
              "amd": "act40_tape",
              "druguom": "tablet",
              "_parentId": "z3Ye07TDj7a",
              "_submissionUid": "z3Ye07TDj7a",
              "prescribed_quantity": 1
            }
          ],
          "referrals": { "cm_measures": "treatment" }
        }
        """;
    JsonNode v1 = mapper.readTree(v1Json);

    JsonNode result = SubmissionNormalizer.normalize(v1, sections, mapper);

    // Values
    JsonNode values = result.get("values");
    assertEquals("2025-09-27", values.get("visitdate").asText());
    assertEquals("treatment", values.get("cm_measures").asText());

    // Collections
    JsonNode collections = result.get("collections");
    assertTrue(collections.has("medicines"));

    JsonNode medicinesMap = collections.get("medicines");
    assertTrue(medicinesMap.isObject(), "medicines should be a map, not an array");

    JsonNode row = medicinesMap.get("01K693VTPPWQR1M23AN06B6N0D");
    assertNotNull(row, "Row should be keyed by _id");
    assertEquals("act40_tape", row.get("amd").asText());
    assertEquals("tablet", row.get("druguom").asText());
    assertEquals(1, row.get("prescribed_quantity").asInt());
    assertEquals(1, row.get("_index").asInt());

    // V1 metadata stripped
    assertFalse(row.has("_id"), "_id should become the map key, not a field");
    assertFalse(row.has("_uid"), "_uid should not be present");
    assertFalse(row.has("_submissionUid"), "_submissionUid should be stripped");

    // _parentId → _parent_id (preserved for round-trip, even if it equals
    // submissionUid)
    assertTrue(row.has("_parent_id"), "_parentId should be converted to _parent_id");
  }

  @Test
  @DisplayName("T3: Empty repeater normalizes to empty collection map")
  void emptyRepeater() throws Exception {
    String v1Json = """
        {
          "main": { "visitdate": "2025-09-27" },
          "medicines": []
        }
        """;
    JsonNode v1 = mapper.readTree(v1Json);

    JsonNode result = SubmissionNormalizer.normalize(v1, sections, mapper);

    JsonNode medicinesMap = result.get("collections").get("medicines");
    assertNotNull(medicinesMap);
    assertTrue(medicinesMap.isObject());
    assertEquals(0, medicinesMap.size(), "Empty array should become empty map");
  }

  @Test
  @DisplayName("T4: Nested repeater preserves _parent_id linking child to parent row")
  void nestedRepeater() throws Exception {
    // Synthetic: households → family_members
    SectionTemplateElementDto households = new SectionTemplateElementDto();
    households.setName("households");
    households.setRepeatable(true);
    households.setOrder(100);

    SectionTemplateElementDto members = new SectionTemplateElementDto();
    members.setName("family_members");
    members.setRepeatable(true);
    members.setOrder(200);

    List<SectionTemplateElementDto> nestedSections = List.of(households, members);

    String v1Json = """
        {
          "households": [
            { "_id": "hh_001", "address": "123 Main St", "_parentId": "sub_uid", "_submissionUid": "sub_uid" }
          ],
          "family_members": [
            { "_id": "mem_001", "name": "John", "age": 45, "_parentId": "hh_001", "_submissionUid": "sub_uid" }
          ]
        }
        """;
    JsonNode v1 = mapper.readTree(v1Json);

    JsonNode result = SubmissionNormalizer.normalize(v1, nestedSections, mapper);

    JsonNode householdsMap = result.get("collections").get("households");
    JsonNode membersMap = result.get("collections").get("family_members");

    assertNotNull(householdsMap.get("hh_001"));
    assertEquals("123 Main St", householdsMap.get("hh_001").get("address").asText());

    assertNotNull(membersMap.get("mem_001"));
    assertEquals("John", membersMap.get("mem_001").get("name").asText());
    // Nested repeater: _parent_id should point to parent ROW id, not submission uid
    assertEquals("hh_001", membersMap.get("mem_001").get("_parent_id").asText());
  }

  @Test
  @DisplayName("T5: Multi-select array values are NOT treated as repeaters")
  void multiSelectArrayValues() throws Exception {
    String v1Json = """
        {
          "patients": {
            "symptoms": ["fever", "headache", "nausea"],
            "lab_test": ["rapid_test_malaria"]
          }
        }
        """;
    JsonNode v1 = mapper.readTree(v1Json);

    JsonNode result = SubmissionNormalizer.normalize(v1, sections, mapper);

    JsonNode values = result.get("values");
    // Multi-select arrays should be preserved as values, not turned into
    // collections
    assertTrue(values.get("symptoms").isArray());
    assertEquals(3, values.get("symptoms").size());
    assertEquals("fever", values.get("symptoms").get(0).asText());

    assertTrue(values.get("lab_test").isArray());
    assertEquals(1, values.get("lab_test").size());
  }

  @Test
  @DisplayName("T6: Null formData produces empty canonical structure")
  void nullFormData() {
    JsonNode result = SubmissionNormalizer.normalize(null, sections, mapper);

    assertNotNull(result);
    assertNotNull(result.get("values"));
    assertNotNull(result.get("collections"));
    assertEquals(0, result.get("values").size());
    assertEquals(0, result.get("collections").size());
  }

  @Test
  @DisplayName("T6b: Null-node formData produces empty canonical structure")
  void nullNodeFormData() {
    JsonNode nullNode = mapper.nullNode();
    JsonNode result = SubmissionNormalizer.normalize(nullNode, sections, mapper);

    assertNotNull(result);
    assertEquals(0, result.get("values").size());
    assertEquals(0, result.get("collections").size());
  }

  @Test
  @DisplayName("Full V1 sample normalizes correctly")
  void fullV1Sample() throws Exception {
    String v1Json = """
        {
          "main": {
            "visitdate": "2025-09-27",
            "NotificationNumber": 2,
            "emergency_team_type": "malaria_unit"
          },
          "patients": {
            "age": "2017-09-26T21:00:00.000Z",
            "gender": "MALE",
            "lab_test": ["الفحص السريع للملاريا"],
            "symptoms": ["حمى", "قشعريرة", "صداع", "تعرق", "قيء"],
            "ispregnant": false,
            "PatientName": "محمد فيصل كامل مشعل",
            "serialNumber": 4,
            "diagnosed_disease_type": "malaria"
          },
          "medicines": [
            {
              "_id": "01K693VTPPWQR1M23AN06B6N0D",
              "amd": "act40_tape",
              "_index": 1,
              "druguom": "tablet",
              "_parentId": "z3Ye07TDj7a",
              "_submissionUid": "z3Ye07TDj7a",
              "prescribeddrug": "PMQ",
              "prescribed_quantity": 1
            }
          ],
          "referrals": {
            "cm_measures": "treatment"
          }
        }
        """;
    JsonNode v1 = mapper.readTree(v1Json);

    JsonNode result = SubmissionNormalizer.normalize(v1, sections, mapper);

    JsonNode values = result.get("values");
    // From 'main' section
    assertEquals("2025-09-27", values.get("visitdate").asText());
    assertEquals(2, values.get("NotificationNumber").asInt());
    assertEquals("malaria_unit", values.get("emergency_team_type").asText());

    // From 'patients' section
    assertEquals("MALE", values.get("gender").asText());
    assertEquals(false, values.get("ispregnant").asBoolean());
    assertEquals("محمد فيصل كامل مشعل", values.get("PatientName").asText());
    assertEquals(4, values.get("serialNumber").asInt());

    // Multi-select preserved as arrays in values
    assertTrue(values.get("symptoms").isArray());
    assertTrue(values.get("lab_test").isArray());

    // From 'referrals' section
    assertEquals("treatment", values.get("cm_measures").asText());

    // Collections
    JsonNode medicines = result.get("collections").get("medicines");
    assertNotNull(medicines);
    assertEquals(1, medicines.size());

    JsonNode row = medicines.get("01K693VTPPWQR1M23AN06B6N0D");
    assertEquals("act40_tape", row.get("amd").asText());
    assertEquals("tablet", row.get("druguom").asText());
    assertEquals("PMQ", row.get("prescribeddrug").asText());
    assertEquals(1, row.get("prescribed_quantity").asInt());
  }
}
