package com.groupMeeting.test.base.util;

import com.groupMeeting.meet.repository.impl.plan.PlanRepositorySupport;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {
    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory(){
        return new JPAQueryFactory(JPQLTemplates.DEFAULT,entityManager);
    }

    @Bean
    public PlanRepositorySupport planRepositorySupport(){
        return new PlanRepositorySupport(jpaQueryFactory());
    }
}
