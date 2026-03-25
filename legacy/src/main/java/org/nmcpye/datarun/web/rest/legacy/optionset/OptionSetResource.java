package org.nmcpye.datarun.web.rest.legacy.optionset;

import org.nmcpye.datarun.jpa.option.OptionSet;
import org.nmcpye.datarun.jpa.option.repository.OptionSetRepository;
import org.nmcpye.datarun.jpa.option.service.OptionSetService;
import org.nmcpye.datarun.iam.security.AuthoritiesConstants;
import org.nmcpye.datarun.web.common.ApiVersion;
import org.nmcpye.datarun.web.rest.legacy.JpaBaseResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.nmcpye.datarun.web.rest.legacy.optionset.OptionSetResource.CUSTOM;

/**
 * Legacy REST controller for managing {@link OptionSet}.
 * Admin/CUSTOM path only — the V1 mobile path is now served by
 * {@link org.nmcpye.datarun.web.rest.v1.optionset.OptionSetResource}.
 */
@RestController("optionSetResourceLegacy")
@RequestMapping(value = { CUSTOM })
@PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.USER + "\")")
public class OptionSetResource
        extends JpaBaseResource<OptionSet> {

    protected static final String NAME = "/optionSets";
    protected static final String CUSTOM = ApiVersion.API_CUSTOM + NAME;
    private final OptionSetRepository optionSetRepository;

    public OptionSetResource(OptionSetService optionSetService,
            OptionSetRepository optionSetRepository) {
        super(optionSetService, optionSetRepository);
        this.optionSetRepository = optionSetRepository;
    }

    // /**
    // * to maintain references indexing. if option set exists in db, looks up
    // payload options by their
    // * un-updatable codes and set their ids to existing id or null if new options
    // *
    // * @param payLoadEntities option set
    // * @return processed option set
    // */
    // @Override
    // protected List<OptionSet> preProcess(List<OptionSet> payLoadEntities) {
    // List<String> incomingKeys = payLoadEntities.stream()
    // .map(OptionSet::getUid)
    // .collect(Collectors.toList());
    //
    // List<OptionSet> existingEntities =
    // optionSetRepository.findAllByUidIn(incomingKeys);
    // JpaIdentifiableOperationVm<OptionSet> operationVm = split(payLoadEntities,
    // existingEntities);
    // return Stream.concat(operationVm.getForUpdateEntities().stream(),
    // operationVm.getForCreateEntities().stream()).toList();
    // }
    //
    // JpaIdentifiableOperationVm<OptionSet> split(List<OptionSet> incomingEntities,
    // List<OptionSet> existingEntities) {
    // Map<String, OptionSet> existingMap = existingEntities.stream()
    // .collect(Collectors.toMap(OptionSet::getUid, Function.identity()));
    // final var builder = JpaIdentifiableOperationVm.<OptionSet>builder();
    // for (OptionSet incomingEntity : incomingEntities) {
    // if (existingMap.containsKey(incomingEntity.getUid())) {
    // // It's an update
    // OptionSet existing = existingMap.get(incomingEntity.getUid());
    // processOptions(incomingEntity, existing);
    // copyProperties(incomingEntity, existing); // Use a utility to copy
    // properties, skipping the ID
    // builder.forUpdateEntity(existing);
    // } else {
    // // It's a creation
    // builder.forCreateEntity(incomingEntity);
    // }
    // }
    // return builder.build();
    // }
    //
    // private void copyProperties(OptionSet incoming, OptionSet existing) {
    // if(incoming != null && existing != null) {
    // if(incoming.getName() != null) {
    // existing.setName(incoming.getName());
    // }
    //
    // existing.setLabel(incoming.getLabel());
    // existing.setProperties(incoming.getProperties());
    // existing.setCode(incoming.getCode());
    // }
    // }
    //
    // private void processOptions(OptionSet incomingEntity, OptionSet existing) {
    // Map<String, String> codeIdMap =
    // existing.getOptionCodePropertyMap(IdScheme.ID);
    // final var options = incomingEntity.getOptions().stream().peek((option) -> {
    // option.setId(codeIdMap.get(option.getCode()));
    // }).toList();
    // incomingEntity.setOptions(options);
    // }

    @Override
    protected String getName() {
        return "optionSets";
    }
}
