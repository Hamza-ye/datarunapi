package org.nmcpye.datarun.web.common;

import org.nmcpye.datarun.acl.AclService;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.iam.security.SecurityUtils;
import org.nmcpye.datarun.sharedkernal.DRunApiVersion;
import org.nmcpye.datarun.sharedkernal.IdentifiableObject;
import org.nmcpye.datarun.sharedkernal.IdentifiableObjectRepository;
import org.nmcpye.datarun.sharedkernal.IdentifiableObjectService;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.mvc.annotation.ApiVersion;
import org.nmcpye.datarun.web.rest.v1.paging.PagingConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

//@RequestMapping("/api")
@ApiVersion({ DRunApiVersion.DEFAULT, DRunApiVersion.ALL })
public abstract class BaseReadResource<T extends IdentifiableObject<ID>, ID extends Serializable> {

    protected final Logger log = LoggerFactory.getLogger(BaseReadResource.class);

    @SuppressWarnings("unused")
    @Value("${jhipster.clientApp.name}")
    protected String applicationName;

    final protected IdentifiableObjectService<T, ID> identifiableObjectService;
    final protected IdentifiableObjectRepository<T, ID> repository;

    @Autowired
    protected AclService aclService;

    protected BaseReadResource(IdentifiableObjectService<T, ID> identifiableObjectService,
            IdentifiableObjectRepository<T, ID> repository) {
        this.identifiableObjectService = identifiableObjectService;
        this.repository = repository;
    }

    protected CrudRepository<T, ID> getRepository() {
        return repository;
    }

//    /**
//     * {@code GET  /Ts} : get all the entities.
//     *
//     * @param queryRequest the query request parameters.
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
//     *         of assignments in body.
//     */
//    @GetMapping("")
//    protected ResponseEntity<PagedResponse<?>> getAll(
//            QueryRequest queryRequest) {
//
//        Page<T> processedPage = getList(queryRequest, null);
//
//        String next = PagingConfigurator.createNextPageLink(processedPage);
//
//        PagedResponse<T> response = PagingConfigurator.initPageResponse(processedPage, next, getName());
//        return ResponseEntity.ok(response);
//    }

    /**
     * minimal Access rights or throw
     *
     * @param currentUser user
     * @throws ResponseStatusException exception if has no business here whatsoever
     *                                 (no minimal rights)
     */
    protected void hasMinimalRightsOrThrow(CurrentUserDetails currentUser) throws ResponseStatusException {
        if (currentUser == null || !aclService.hasMinimalRights(currentUser)) {
            log.warn("REST Prevent Access, no minimal rights `{}`:`{}`", getEntityClass().getSimpleName(), currentUser);
            throw new AccessDeniedException(HttpStatus.FORBIDDEN + ", You Hava No Business Here");
        }
    }

    @PostMapping("/query")
    public ResponseEntity<PagedResponse<?>> unifiedMongoLikeQuerying(QueryRequest queryRequest,
            @RequestBody(required = false) String jsonQuery) {
        try {
            Page<T> processedPage = getList(queryRequest, jsonQuery);

            String next = PagingConfigurator.createNextPageLink(processedPage);

            PagedResponse<T> response = PagingConfigurator.initPageResponse(processedPage, next, getName());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private Class<T> entityClass;

    @SuppressWarnings("unchecked")
    protected final Class<T> getEntityClass() {
        if (entityClass == null) {
            Type[] actualTypeArguments = ((ParameterizedType) getClass().getGenericSuperclass())
                    .getActualTypeArguments();
            entityClass = (Class<T>) actualTypeArguments[0];
        }

        return entityClass;
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<T> getById(@PathVariable("id") String id,
//            @AuthenticationPrincipal CurrentUserDetails user) {
//        hasMinimalRightsOrThrow(user);
//        log.debug("REST request to get from {}: {}", getName(), id);
//        Optional<T> entity = identifiableObjectService.findByIdOrUid(id);
//        return ResponseUtil.wrapOrNotFound(entity);
//    }

    protected Page<T> getList(QueryRequest queryRequest, String jsonQueryBody) {
        final var user = SecurityUtils.getCurrentUserDetailsOrThrow();
        log.debug("REST request to getList {}:{}", user.getUsername(), getName());
        if (!aclService.hasMinimalRights(user)) {
            log.warn("REST Prevent Access to `{}`, no minimal rights: `{}`", getEntityClass().getSimpleName(), user);
            return Page.empty();
        }

        return identifiableObjectService.findAllByUser(queryRequest, jsonQueryBody)
                .map(s -> postProcess(s, queryRequest, jsonQueryBody));
    }

    protected T postProcess(T entity, QueryRequest queryRequest, String jsonQuery) {
        return entity;
    }

    protected abstract String getName();
}
