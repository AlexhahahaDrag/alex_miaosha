package com.alex.api.user.handler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrgSubtreeLookupConfiguration {

    @Bean
    @ConditionalOnMissingBean(OrgSubtreeLookup.class)
    public OrgSubtreeLookup orgSubtreeLookup() {
        return OrgSubtreeLookup.NOOP;
    }
}
