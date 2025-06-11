package com.groupMeeting.core.mapping;

import com.groupMeeting.core.annotation.version.ApiVersion;
import com.groupMeeting.policy.service.ApiVersionPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

@Component
@RequiredArgsConstructor
public class ApiVersionHandlerMapping extends RequestMappingHandlerMapping {
    private final ApiVersionPolicyService apiVersionPolicyService;

    @Override
    protected boolean isHandler(Class<?> beanType) {
        return super.isHandler(beanType);
    }

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo mappingInfo = super.getMappingForMethod(method, handlerType);

        if (mappingInfo == null) {
            return null;
        }

        ApiVersion methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, ApiVersion.class);
        ApiVersion classAnnotation = AnnotatedElementUtils.findMergedAnnotation(handlerType, ApiVersion.class);
        ApiVersion apiVersion = methodAnnotation != null ? methodAnnotation : classAnnotation;

        if (apiVersion != null) {
            RequestCondition<?> condition = new ApiVersionCondition(apiVersionPolicyService, apiVersion.value());

            return RequestMappingInfo
                    .paths()
                    .customCondition(condition)
                    .build()
                    .combine(mappingInfo);
        }

        return mappingInfo;
    }
}
