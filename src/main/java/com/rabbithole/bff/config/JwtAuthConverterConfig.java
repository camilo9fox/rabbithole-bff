package com.rabbithole.bff.config;

import java.util.Collection;
import java.util.HashSet;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import reactor.core.publisher.Flux;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class JwtAuthConverterConfig {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthConverterConfig.class);

    @Bean
    public ReactiveJwtAuthenticationConverter reactiveJwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

        Converter<Jwt, Flux<GrantedAuthority>> authoritiesConverter = jwt -> {
            Collection<GrantedAuthority> authorities = new HashSet<>(defaultConverter.convert(jwt));

            // Detect job title in various claim names / formats
            String jobTitle = jwt.getClaimAsString("jobTitle");
            if (jobTitle == null) {
                jobTitle = jwt.getClaimAsString("extension_jobTitle");
            }
            if (jobTitle == null && jwt.hasClaim("jobTitle")) {
                List<String> titles = jwt.getClaimAsStringList("jobTitle");
                if (titles != null && !titles.isEmpty()) {
                    jobTitle = titles.get(0);
                }
            }

            if ("Admin".equalsIgnoreCase(jobTitle)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }

            log.debug("JWT jobTitle={}, resulting authorities={}", jobTitle, authorities);
            return Flux.fromIterable(authorities);
        };

        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }
}