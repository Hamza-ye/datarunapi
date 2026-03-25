package org.nmcpye.datarun.template.dataelement.service;

import org.nmcpye.datarun.jpa.accessfilter.UserAccessService;
import org.nmcpye.datarun.template.dataelement.DataElement;
import org.nmcpye.datarun.template.dataelement.repository.DataElementRepository;
import org.nmcpye.datarun.jpa.option.repository.OptionSetRepository;
import org.nmcpye.datarun.sharedkernal.DefaultJpaIdentifiableService;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Hamza Assada
 * @since 08/02/2024
 */
@Service
@Transactional
public class DefaultDataElementService extends DefaultJpaIdentifiableService<DataElement> implements DataElementService {
    private final OptionSetRepository optionSetRepository;

    public DefaultDataElementService(DataElementRepository repository, CacheManager cacheManager,
                                     OptionSetRepository optionSetRepository,
                                     UserAccessService userAccessService) {
        super(repository, cacheManager, userAccessService);
        this.optionSetRepository = optionSetRepository;
    }

    @Override
    public DataElement saveWithRelations(DataElement element) {
        if (element.getValueType().isOptionsType() && element.getOptionSet() != null) {
            final var optionSet = optionSetRepository.findByUid(element.getOptionSet().getUid())
                .or(() -> optionSetRepository
                    .findById(element.getOptionSet()
                        .getId()))
                .orElseThrow(() -> new IllegalStateException(element.getName() + "'s Option Set not found"));
            element.setOptionSet(optionSet);
        }
        return save(element);
    }
}
