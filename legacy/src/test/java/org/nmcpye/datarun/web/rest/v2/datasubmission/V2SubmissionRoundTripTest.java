package org.nmcpye.datarun.web.rest.v2.datasubmission;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;
import org.nmcpye.datarun.analytics.etl.model.TemplateElementMap;
import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;
import org.nmcpye.datarun.datacapture.datasubmission.service.DataSubmissionService;
import org.nmcpye.datarun.datacapture.datasubmission.validation.CompositeSubmissionValidator;
import org.nmcpye.datarun.datacapture.datasubmission.validation.SubmissionAccessValidator;
import org.nmcpye.datarun.template.datatemplate.TemplateVersion;
import org.nmcpye.datarun.template.datatemplate.repository.TemplateVersionRepository;
import org.nmcpye.datarun.template.datatemplate.service.TemplateElementService;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.iam.security.SecurityUtils;
import org.nmcpye.datarun.web.rest.v2.datasubmission.service.SubmissionTranslationService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration Test ensuring the full V2 POST -> ACL -> Storage -> ACL -> V2 GET
 * round-trip works.
 * Wires the real REST Controller with the real SubmissionTranslationService,
 * SubmissionNormalizer, and SubmissionDenormalizer.
 * Only the database/JPA layers are mocked.
 */
class V2SubmissionRoundTripTest {

      private MockMvc mockMvc;
      private final ObjectMapper objectMapper = new ObjectMapper();

      private DataSubmissionService submissionService;
      private TemplateVersionRepository templateVersionRepository;
      private CompositeSubmissionValidator compositeValidator;
      private SubmissionAccessValidator submissionAccessValidator;
      private TemplateElementService templateElementService;

      @BeforeEach
      void setUp() {
            submissionService = mock(DataSubmissionService.class);
            templateVersionRepository = mock(TemplateVersionRepository.class);
            compositeValidator = mock(CompositeSubmissionValidator.class);
            submissionAccessValidator = mock(SubmissionAccessValidator.class);
            templateElementService = mock(TemplateElementService.class);

            // Wire real ACL service
            SubmissionTranslationService translationService = new SubmissionTranslationService(
                        templateVersionRepository,
                        objectMapper);

            V2DataSubmissionResource controller = new V2DataSubmissionResource(
                        submissionService,
                        translationService,
                        objectMapper,
                        compositeValidator,
                        submissionAccessValidator,
                        templateElementService);

            mockMvc = MockMvcBuilders.standaloneSetup(controller)
                        .setValidator(mock(org.springframework.validation.Validator.class))
                        .build();
      }

      @Test
      @DisplayName("V2 POST saves V1 shape, then V2 GET restores original canonical shape")
      void canonicalV2PostToV1StorageToCanonicalV2Get() throws Exception {
            // 1. Arrange: Define a TemplateVersion with one main section and one repeater
            // section
            TemplateVersion version = new TemplateVersion();
            version.setUid("ver01");

            SectionTemplateElementDto mainSection = new SectionTemplateElementDto();
            mainSection.setName("main");
            mainSection.setRepeatable(false);

            SectionTemplateElementDto repeatSection = new SectionTemplateElementDto();
            repeatSection.setName("medicines");
            repeatSection.setRepeatable(true);

            version.setSections(List.of(mainSection, repeatSection));

            FieldTemplateElementDto visitdateField = new FieldTemplateElementDto();
            visitdateField.setName("visitdate");
            visitdateField.setParent("main");
            version.setFields(List.of(visitdateField));

            when(templateVersionRepository.findByUid("ver01")).thenReturn(Optional.of(version));

            // Mock empty template access details to pass preProcess generators safely
            when(templateElementService.getTemplateElementMap(anyString(), anyString()))
                        .thenReturn(mock(TemplateElementMap.class));

            try (org.mockito.MockedStatic<SecurityUtils> securityUtilsMock = org.mockito.Mockito
                        .mockStatic(SecurityUtils.class)) {
                  CurrentUserDetails mockUser = mock(CurrentUserDetails.class);
                  securityUtilsMock.when(SecurityUtils::getCurrentUserDetailsOrThrow).thenReturn(mockUser);

                  // Construct Canonical V2 payload (§4 shape)
                  String canonicalPayload = """
                              {
                                 "template_uid": "tmpl01",
                                 "version_uid": "ver01",
                                 "submission_uid": "sub001",
                                 "values": {
                                    "visitdate": "2025-09-27"
                                 },
                                 "collections": {
                                    "medicines": {
                                       "row_1": { "drug": "Paracetamol" },
                                       "row_2": { "drug": "Ibuprofen" }
                                    }
                                 }
                              }
                              """;

                  // Mock saving the entity — return it as is, but it should contain V1 formData
                  // parsed by the controller
                  ArgumentCaptor<DataSubmission> captor = ArgumentCaptor.forClass(DataSubmission.class);
                  when(submissionService.upsert(captor.capture(), any(), any()))
                              .thenAnswer(invocation -> invocation.getArgument(0));

                  // 2. Act: POST canonical shape to V2 endpoint
                  mockMvc.perform(post("/api/v2/dataSubmission")
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(canonicalPayload))
                              .andExpect(status().isCreated())
                              .andExpect(jsonPath("$.values.visitdate").value("2025-09-27"))
                              .andExpect(jsonPath("$.collections.medicines.row_1.drug").value("Paracetamol"));

                  // Verify the denormalized V1 shape was stored in the captured DataSubmission
                  // entity!
                  DataSubmission savedEntity = captor.getValue();
                  assertNotNull(savedEntity);
                  JsonNode savedFormData = savedEntity.getFormData();
                  assertNotNull(savedFormData);

                  // Assert V1 shape rules: sections as wrappers, collections as arrays
                  assertNotNull(savedFormData.get("main"));
                  assertEquals("2025-09-27", savedFormData.get("main").get("visitdate").asText());

                  assertNotNull(savedFormData.get("medicines"));
                  assertEquals(2, savedFormData.get("medicines").size()); // Array of 2
                  assertEquals("row_1", savedFormData.get("medicines").get(0).get("_id").asText());

                  // 3. Act: Emulate GET /api/v2/dataSubmission/sub001
                  when(submissionService.findByUid("sub001")).thenReturn(Optional.of(savedEntity));

                  mockMvc.perform(get("/api/v2/dataSubmission/sub001"))
                              .andExpect(status().isOk())
                              .andExpect(jsonPath("$.values.visitdate").value("2025-09-27"))
                              // Collections mapped back to dictionary matching canonical payload
                              .andExpect(jsonPath("$.collections.medicines.row_1.drug").value("Paracetamol"))
                              .andExpect(jsonPath("$.collections.medicines.row_2.drug").value("Ibuprofen"));
            }
      }
}
