package org.nmcpye.datarun.config;

import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.hibernate.cache.jcache.ConfigSettings;
import org.nmcpye.datarun.analytics.etl.service.impl.RefTypeValueResolutionService;
import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;
import org.nmcpye.datarun.assignment.activity.Activity;
import org.nmcpye.datarun.assignment.assignment.Assignment;
import org.nmcpye.datarun.template.dataelement.DataElement;
import org.nmcpye.datarun.template.datatemplate.DataTemplate;
import org.nmcpye.datarun.template.datatemplate.TemplateElement;
import org.nmcpye.datarun.template.datatemplate.TemplateVersion;
import org.nmcpye.datarun.template.datatemplate.repository.DataTemplateRepository;
import org.nmcpye.datarun.template.datatemplate.repository.TemplateVersionRepository;
import org.nmcpye.datarun.template.datatemplate.service.TemplateElementService;
import org.nmcpye.datarun.jpa.option.Option;
import org.nmcpye.datarun.jpa.option.OptionGroup;
import org.nmcpye.datarun.jpa.option.OptionGroupSet;
import org.nmcpye.datarun.jpa.option.OptionSet;
import org.nmcpye.datarun.jpa.orgunit.OrgUnit;
import org.nmcpye.datarun.jpa.orgunit.OrgUnitGroup;
import org.nmcpye.datarun.jpa.orgunit.OrgUnitGroupSet;
import org.nmcpye.datarun.jpa.orgunit.OuLevel;
import org.nmcpye.datarun.assignment.project.Project;
import org.nmcpye.datarun.iam.team.Team;
import org.nmcpye.datarun.iam.usegroup.UserGroup;
import org.nmcpye.datarun.iam.user.User;
import org.nmcpye.datarun.iam.user.repository.UserRepository;
import org.nmcpye.datarun.iam.userauthority.Authority;
import org.nmcpye.datarun.iam.userole.Privilege;
import org.nmcpye.datarun.iam.userole.Role;

import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfiguration {

    private final javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration;

    public CacheConfiguration(DataRunProperties dataRunProperties) {
        DataRunProperties.Cache.Ehcache ehcache = dataRunProperties.getCache().getEhcache();

        jcacheConfiguration = Eh107Configuration.fromEhcacheCacheConfiguration(
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        Object.class,
                        Object.class,
                        ResourcePoolsBuilder.heap(ehcache.getMaxEntries()))
                        .withExpiry(ExpiryPolicyBuilder
                                .timeToLiveExpiration(Duration.ofSeconds(ehcache.getTimeToLiveSeconds())))
                        .build());
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(javax.cache.CacheManager cacheManager) {
        return hibernateProperties -> hibernateProperties.put(ConfigSettings.CACHE_MANAGER, cacheManager);
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cm -> {
            createCache(cm, UserRepository.USERS_BY_LOGIN_CACHE);
            createCache(cm, UserRepository.USERS_BY_EMAIL_CACHE);
            createCache(cm, UserRepository.USER_TEAM_IDS_CACHE);
            createCache(cm, UserRepository.USER_GROUP_IDS_CACHE);
            createCache(cm, UserRepository.USER_ACTIVITY_IDS_CACHE);
            createCache(cm, UserRepository.USER_TEAM_FORM_ACCESS_CACHE);
            createCache(cm, TemplateElementService.TEMPLATE_MAP_CACHE);
            createCache(cm, DataTemplateRepository.TEMPLATE_BY_UID_CACHE);
            createCache(cm, TemplateVersionRepository.TEMPLATE_UID_VERSION_NO_JPA_CACHE);
            createCache(cm, TemplateVersionRepository.TEMPLATE_UID_VERSION_UID_JPA_CACHE);
            createCache(cm, TemplateVersionRepository.TEMPLATE_UID_LATEST_VERSION_JPA_CACHE);
            createCache(cm, User.class.getName());
            createCache(cm, User.class.getName());
            createCache(cm, Authority.class.getName());
            createCache(cm, Role.class.getName());
            createCache(cm, Privilege.class.getName());
            createCache(cm, User.class.getName() + ".authorities");
            createCache(cm, User.class.getName() + ".teams");
            createCache(cm, User.class.getName() + ".roles");
            createCache(cm, User.class.getName() + ".userGroups");
            createCache(cm, Role.class.getName() + ".privileges");
            createCache(cm, Privilege.class.getName() + ".roles");

            createCache(cm, UserGroup.class.getName());
            createCache(cm, UserGroup.class.getName() + ".users");
            createCache(cm, UserGroup.class.getName() + ".managedByGroups");
            createCache(cm, UserGroup.class.getName() + ".managedGroups");

            createCache(cm, Project.class.getName());
            createCache(cm, Project.class.getName() + ".activities");
            createCache(cm, Activity.class.getName());
            createCache(cm, Activity.class.getName() + ".assignments");

            createCache(cm, Assignment.class.getName());
            createCache(cm, Assignment.class.getName() + ".children");

            createCache(cm, Team.class.getName());
            createCache(cm, Team.class.getName() + ".assignments");
            createCache(cm, Team.class.getName() + ".users");
            createCache(cm, Team.class.getName() + ".managedTeams");
            createCache(cm, Team.class.getName() + ".managedByTeams");

            createCache(cm, OrgUnit.class.getName());
            createCache(cm, OrgUnit.class.getName() + ".assignments");
            createCache(cm, OrgUnit.class.getName() + ".orgUnitGroups");
            createCache(cm, OrgUnit.class.getName() + ".children");
            createCache(cm, OuLevel.class.getName());

            createCache(cm, OrgUnitGroup.class.getName());
            createCache(cm, OrgUnitGroup.class.getName() + ".orgUnits");
            createCache(cm, OrgUnitGroup.class.getName() + ".orgUnitGroupSets");
            createCache(cm, OrgUnitGroupSet.class.getName());
            createCache(cm, OrgUnitGroupSet.class.getName() + ".orgUnitGroups");
            createCache(cm, DataElement.class.getName());
            createCache(cm, DataElement.class.getName() + ".dataElementGroups");
            createCache(cm, DataTemplate.class.getName());

            createCache(cm, Option.class.getName());

            createCache(cm, OptionSet.class.getName());
            createCache(cm, OptionSet.class.getName() + ".options");

            createCache(cm, OptionGroup.class.getName());
            createCache(cm, OptionGroup.class.getName() + ".options");

            createCache(cm, OptionGroupSet.class.getName());
            createCache(cm, OptionGroupSet.class.getName() + ".optionGroups");

            createCache(cm, TemplateElement.class.getName());
            createCache(cm, TemplateVersion.class.getName());

            createCache(cm, DataSubmission.class.getName());
            createCache(cm, RefTypeValueResolutionService.REF_RESOLUTION_CACHE_NAME);
        };
    }

    private void createCache(javax.cache.CacheManager cm, String cacheName) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        } else {
            cm.createCache(cacheName, jcacheConfiguration);
        }
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return new org.springframework.cache.interceptor.SimpleKeyGenerator();
    }
}
