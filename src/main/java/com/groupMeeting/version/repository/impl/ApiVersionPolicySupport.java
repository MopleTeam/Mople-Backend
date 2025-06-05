package com.groupMeeting.version.repository.impl;

import com.groupMeeting.entity.version.ApiVersionPolicy;
import com.groupMeeting.entity.version.QApiVersionPolicy;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ApiVersionPolicySupport {
    private final JPAQueryFactory queryFactory;

    public Optional<ApiVersionPolicy> findApplicablePolicy(String os, String requestUri, int userVersion) {
        QApiVersionPolicy apiVersionPolicy = QApiVersionPolicy.apiVersionPolicy;

        List<ApiVersionPolicy> apiVersionPolicyList = queryFactory
                .selectFrom(apiVersionPolicy)
                .where(
                        apiVersionPolicy.os.eq(os),
                        apiVersionPolicy.appVersion.loe(userVersion)
                )
                .fetch();

        return apiVersionPolicyList.stream()
                .filter(policy -> requestUri.startsWith(policy.getUri()))
                .max(Comparator.comparingInt(policy -> policy.getUri().length()));
    }

    public List<ApiVersionPolicy> findAllByOs(String os) {
        QApiVersionPolicy apiVersionPolicy = QApiVersionPolicy.apiVersionPolicy;

        return queryFactory
                .selectFrom(apiVersionPolicy)
                .where(apiVersionPolicy.os.eq(os))
                .orderBy(apiVersionPolicy.uri.asc(), apiVersionPolicy.appVersion.desc())
                .fetch();
    }
}
