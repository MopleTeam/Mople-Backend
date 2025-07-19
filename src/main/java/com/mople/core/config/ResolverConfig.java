package com.mople.core.config;

import com.mople.core.resolver.AccessTokenResolver;
import com.mople.core.resolver.AuthorizedUserResolver;
import com.mople.core.resolver.RefreshTokenResolver;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class ResolverConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthorizedUserResolver());
        resolvers.add(new AccessTokenResolver());
        resolvers.add(new RefreshTokenResolver());
    }
}
