package org.nmcpye.datarun.web.rest.legacy.metadataschema;

import lombok.Data;

import org.nmcpye.datarun.iam.security.AuthoritiesConstants;
import org.nmcpye.datarun.sharedkernal.IdentifiableObject;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.ApiVersion;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.paging.PagingConfigurator;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

import static org.nmcpye.datarun.web.rest.legacy.metadataschema.MetadataSubmissionResourceCustom.CUSTOM;
import static org.nmcpye.datarun.web.rest.legacy.metadataschema.MetadataSubmissionResourceCustom.V1;

/**
 * REST controller for managing {@link MetadataSubmission}.
 */
@RestController
@RequestMapping(value = {CUSTOM, V1})
@PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.USER + "\")")
public class MetadataSubmissionResourceCustom {
    protected static final String NAME = "/metadataSubmissions";
    protected static final String CUSTOM = ApiVersion.API_CUSTOM + NAME;
    protected static final String V1 = ApiVersion.API_V1 + NAME;

    @Data
    static protected class MetadataSubmission implements IdentifiableObject<String> {
        private String id;
        private String code;
        private String uid;
        private String name;
        private String createdBy;
        private Instant createdDate;
        private Instant lastModifiedDate;
        private String lastModifiedBy;
    }

    @GetMapping("")
    protected ResponseEntity<PagedResponse<?>> getAll(QueryRequest queryRequest) {

        Page<MetadataSubmission> processedPage = Page.empty();

        String next = PagingConfigurator.createNextPageLink(processedPage);

        PagedResponse<MetadataSubmission> response = PagingConfigurator.initPageResponse(processedPage, next, getName());
        return ResponseEntity.ok(response);
    }

    protected String getName() {
        return "metadataSubmissions";
    }
}
