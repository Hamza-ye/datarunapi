package org.nmcpye.datarun.web.rest.v1.account;

import org.nmcpye.datarun.iam.user.repository.UserRepository;
import org.nmcpye.datarun.iam.user.service.UserService;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.web.common.ApiVersion;
import org.nmcpye.datarun.web.rest.legacy.user.AccountResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.nmcpye.datarun.web.rest.v1.account.AccountResourceV1.V1;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping(V1)
public class AccountResourceV1 extends AccountResource {
    protected static final String NAME = "";
    protected static final String V1 = ApiVersion.API_V1 + NAME;

    private final Logger log = LoggerFactory.getLogger(AccountResourceV1.class);

    public AccountResourceV1(UserRepository userRepository, UserService userService) {
        super(userRepository, userService);
    }

    /**
     * {@code GET /authenticate} : check if the user is authenticated, and return its details.
     *
     * @param user the Authenticated principal request.
     * @return the user details if the user is authenticated.
     */
    @GetMapping("/myDetails")
    public CurrentUserDetails getMyDetails(@AuthenticationPrincipal CurrentUserDetails user) {
        log.debug("REST request to check if the current user is authenticated");
        return user;
    }
}
