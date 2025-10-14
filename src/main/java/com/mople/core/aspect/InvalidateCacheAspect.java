package com.mople.core.aspect;

import com.mople.core.annotation.cache.InvalidateCache;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static com.mople.global.utils.transaction.AfterCommit.afterCommit;

@Aspect
@Component
@RequiredArgsConstructor
public class InvalidateCacheAspect {

    private final CacheManager cacheManager;
    private final ApplicationContext applicationContext;

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @AfterReturning(pointcut = "@annotation(invalidate)", returning = "ret")
    public void scheduleEvict(JoinPoint joinPoint, InvalidateCache invalidate, Object ret) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(joinPoint.getTarget(), method, joinPoint.getArgs(), discoverer);

        context.setBeanResolver(new BeanFactoryResolver(applicationContext));

        HashSet<Object> keys = new HashSet<>();
        for (String key : invalidate.keys()) {
            if (key == null || key.isBlank()) {
                continue;
            }

            Object value = parser.parseExpression(key).getValue(context);
            collectKeys(value, keys);
        }

        if (keys.isEmpty()) {
            return;
        }

        String cacheName = invalidate.cacheName();
        Cache cache = cacheManager.getCache(cacheName);

        if (cache == null) {
            return;
        }

        Runnable evictTask = () -> keys.forEach(cache::evictIfPresent);

        afterCommit(evictTask);
    }

    @SuppressWarnings("unchecked")
    private void collectKeys(Object value, Set<Object> keys) {
        if (value == null) {
            return;
        }

        if (value instanceof Iterable<?> iterable) {
            iterable.forEach(keys::add);
            return;
        }
        if (value.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(value); i++) {
                keys.add(Array.get(value, i));
            }
            return;
        }

        keys.add(value);
    }
}
