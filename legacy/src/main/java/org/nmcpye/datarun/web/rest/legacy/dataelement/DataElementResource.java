package org.nmcpye.datarun.web.rest.legacy.dataelement;

import org.nmcpye.datarun.template.dataelement.DataElement;
import org.nmcpye.datarun.template.dataelement.repository.DataElementRepository;
import org.nmcpye.datarun.template.dataelement.service.DataElementService;
import org.nmcpye.datarun.iam.security.AuthoritiesConstants;
import org.nmcpye.datarun.web.common.ApiVersion;
import org.nmcpye.datarun.web.rest.legacy.JpaBaseResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.nmcpye.datarun.web.rest.legacy.dataelement.DataElementResource.CUSTOM;

/**
 * Legacy REST controller for managing {@link DataElement}.
 * Admin/CUSTOM path only — the V1 mobile path is now served by
 * {@link org.nmcpye.datarun.web.rest.v1.dataelement.DataElementResource}.
 */
@RestController("dataElementResourceLegacy")
@RequestMapping(value = { CUSTOM })
@PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.USER + "\")")
public class DataElementResource
        extends JpaBaseResource<DataElement> {
    protected static final String NAME = "/dataElements";
    protected static final String CUSTOM = ApiVersion.API_CUSTOM + NAME;

    public DataElementResource(DataElementService metadataSubmissionService,
            DataElementRepository metadataSchemaRepository) {
        super(metadataSubmissionService, metadataSchemaRepository);
    }

    @Override
    protected String getName() {
        return "dataElements";
    }
}
