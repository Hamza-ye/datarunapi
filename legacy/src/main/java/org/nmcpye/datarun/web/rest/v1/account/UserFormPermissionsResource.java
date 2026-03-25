package org.nmcpye.datarun.web.rest.v1.account;

import lombok.extern.slf4j.Slf4j;
import org.nmcpye.datarun.iam.security.AuthoritiesConstants;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.iam.security.SecurityUtils;
import org.nmcpye.datarun.iam.userdetail.UserFormAccess;
import org.nmcpye.datarun.web.common.ApiVersion;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.paging.PagingConfigurator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.nmcpye.datarun.web.rest.v1.account.AccountResourceV1.V1;

/**
 * REST Extended controller for managing {@link UserFormAccess}.
 */
@RestController
@RequestMapping(V1)
@PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.USER + "\")")
@Slf4j
public class UserFormPermissionsResource {
    protected static final String V1 = ApiVersion.API_V1;

    /**
     * {@code GET  /Ts} : get all the entities.
     *
     * @param user authenticated user.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of items in body.
     */
    @GetMapping("/formPermissions")
    protected ResponseEntity<PagedResponse<?>> getAll(@AuthenticationPrincipal CurrentUserDetails user) throws Exception {
        final var userLogin = SecurityUtils.getCurrentUserDetailsOrThrow();
        log.debug("REST request to getAll {}:{}", userLogin, getName());


        Page<UserFormAccess> processedPage = new PageImpl<>(user.getFormAccess());

        String next = PagingConfigurator.createNextPageLink(processedPage);

        PagedResponse<UserFormAccess> response = PagingConfigurator.initPageResponse(processedPage, next, getName());
        return ResponseEntity.ok(response);
    }

    protected String getName() {
        return "formPermissions";
    }
}
