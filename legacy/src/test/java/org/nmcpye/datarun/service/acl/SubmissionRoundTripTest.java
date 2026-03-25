package org.nmcpye.datarun.service.acl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;
import org.nmcpye.datarun.web.rest.v2.datasubmission.service.SubmissionDenormalizer;
import org.nmcpye.datarun.web.rest.v2.datasubmission.service.SubmissionNormalizer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Round-trip identity tests: V1 → normalize → denormalize → V1.
 * <p>
 * The critical invariant: after a full round-trip, the output must be
 * structurally equivalent to the input. This guarantees that the V1 mobile app
 * sees identical data whether or not the canonical storage is active.
 */
class SubmissionRoundTripTest {

    private ObjectMapper mapper;
    private List<SectionTemplateElementDto> sections;
    private List<FieldTemplateElementDto> fields;
    private static final String SUBMISSION_UID = "z3Ye07TDj7a";

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();

        // Sections
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

        // Fields
        fields = List.of(
                buildField("visitdate", "main"),
                buildField("NotificationNumber", "main"),
                buildField("emergency_team_type", "main"),
                buildField("age", "patients"),
                buildField("gender", "patients"),
                buildField("lab_test", "patients"),
                buildField("symptoms", "patients"),
                buildField("ispregnant", "patients"),
                buildField("PatientName", "patients"),
                buildField("serialNumber", "patients"),
                buildField("diagnosed_disease_type", "patients"),
                buildField("cm_measures", "referrals"));
    }

    private FieldTemplateElementDto buildField(String name, String parent) {
        FieldTemplateElementDto field = new FieldTemplateElementDto();
        field.setName(name);
        field.setParent(parent);
        return field;
    }

    @Test
    @DisplayName("Round-trip: full V1 sample → normalize → denormalize == original (field-level)")
    void fullV1SampleRoundTrip() throws Exception {
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
        JsonNode v1Input = mapper.readTree(v1Json);

        // Step 1: Normalize
        JsonNode canonical = SubmissionNormalizer.normalize(v1Input, sections, mapper);

        // Step 2: Denormalize back
        JsonNode v1Output = SubmissionDenormalizer.denormalize(
                canonical, sections, fields, SUBMISSION_UID, mapper);

        // ── Verify field-level equivalence ──

        // main section
        assertEquals(
                v1Input.get("main").get("visitdate").asText(),
                v1Output.get("main").get("visitdate").asText());
        assertEquals(
                v1Input.get("main").get("NotificationNumber").asInt(),
                v1Output.get("main").get("NotificationNumber").asInt());
        assertEquals(
                v1Input.get("main").get("emergency_team_type").asText(),
                v1Output.get("main").get("emergency_team_type").asText());

        // patients section
        assertEquals(
                v1Input.get("patients").get("gender").asText(),
                v1Output.get("patients").get("gender").asText());
        assertEquals(
                v1Input.get("patients").get("PatientName").asText(),
                v1Output.get("patients").get("PatientName").asText());
        assertEquals(
                v1Input.get("patients").get("ispregnant").asBoolean(),
                v1Output.get("patients").get("ispregnant").asBoolean());
        assertEquals(
                v1Input.get("patients").get("serialNumber").asInt(),
                v1Output.get("patients").get("serialNumber").asInt());

        // Multi-select arrays preserved
        assertEquals(
                v1Input.get("patients").get("symptoms").size(),
                v1Output.get("patients").get("symptoms").size());

        // referrals section
        assertEquals(
                v1Input.get("referrals").get("cm_measures").asText(),
                v1Output.get("referrals").get("cm_measures").asText());

        // medicines repeater
        assertTrue(v1Output.get("medicines").isArray());
        assertEquals(1, v1Output.get("medicines").size());

        JsonNode inputRow = v1Input.get("medicines").get(0);
        JsonNode outputRow = v1Output.get("medicines").get(0);

        assertEquals(inputRow.get("_id").asText(), outputRow.get("_id").asText());
        assertEquals(inputRow.get("amd").asText(), outputRow.get("amd").asText());
        assertEquals(inputRow.get("druguom").asText(), outputRow.get("druguom").asText());
        assertEquals(inputRow.get("prescribeddrug").asText(), outputRow.get("prescribeddrug").asText());
        assertEquals(inputRow.get("prescribed_quantity").asInt(), outputRow.get("prescribed_quantity").asInt());
        assertEquals(inputRow.get("_index").asInt(), outputRow.get("_index").asInt());
        assertEquals(inputRow.get("_parentId").asText(), outputRow.get("_parentId").asText());
        assertEquals(inputRow.get("_submissionUid").asText(), outputRow.get("_submissionUid").asText());
    }

    @Test
    @DisplayName("Round-trip: empty repeater")
    void emptyRepeaterRoundTrip() throws Exception {
        String v1Json = """
                {
                  "main": { "visitdate": "2025-09-27" },
                  "medicines": [],
                  "patients": {},
                  "referrals": {}
                }
                """;
        JsonNode v1Input = mapper.readTree(v1Json);

        JsonNode canonical = SubmissionNormalizer.normalize(v1Input, sections, mapper);
        JsonNode v1Output = SubmissionDenormalizer.denormalize(
                canonical, sections, fields, SUBMISSION_UID, mapper);

        assertEquals("2025-09-27", v1Output.get("main").get("visitdate").asText());
        assertTrue(v1Output.get("medicines").isArray());
        assertEquals(0, v1Output.get("medicines").size());
    }

    @Test
    @DisplayName("Round-trip: nested repeaters preserve parent linkage")
    void nestedRepeatersRoundTrip() throws Exception {
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
                    { "_id": "hh_001", "address": "123 Main St", "_parentId": "sub_uid", "_submissionUid": "sub_uid", "_index": 1 },
                    { "_id": "hh_002", "address": "456 Side St", "_parentId": "sub_uid", "_submissionUid": "sub_uid", "_index": 2 }
                  ],
                  "family_members": [
                    { "_id": "mem_001", "name": "John", "age": 45, "_parentId": "hh_001", "_submissionUid": "sub_uid", "_index": 1 },
                    { "_id": "mem_002", "name": "Jane", "age": 42, "_parentId": "hh_001", "_submissionUid": "sub_uid", "_index": 2 },
                    { "_id": "mem_003", "name": "Bob", "age": 20, "_parentId": "hh_002", "_submissionUid": "sub_uid", "_index": 1 }
                  ]
                }
                """;
        JsonNode v1Input = mapper.readTree(v1Json);

        JsonNode canonical = SubmissionNormalizer.normalize(v1Input, nestedSections, mapper);
        JsonNode v1Output = SubmissionDenormalizer.denormalize(
                canonical, nestedSections, List.of(), "sub_uid", mapper);

        // Verify household count
        assertEquals(2, v1Output.get("households").size());
        // Verify member count
        assertEquals(3, v1Output.get("family_members").size());

        // Verify parent linkage preserved for nested repeater
        // Find mem_001 and check its _parentId
        boolean foundMem001 = false;
        for (JsonNode member : v1Output.get("family_members")) {
            if ("mem_001".equals(member.get("_id").asText())) {
                assertEquals("hh_001", member.get("_parentId").asText());
                assertEquals("sub_uid", member.get("_submissionUid").asText());
                foundMem001 = true;
            }
        }
        assertTrue(foundMem001, "mem_001 should be present in output");
    }
}
