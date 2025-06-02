package com.groupMeeting.test.unit.global.resolver;

import com.groupMeeting.dto.request.user.AuthUserRequest;
import com.groupMeeting.core.annotation.auth.SignUser;
import com.groupMeeting.core.resolver.AuthorizedUserResolver;
import com.groupMeeting.test.base.object.MockitoTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.core.MethodParameter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class AuthorizedUserResolverTest extends MockitoTest {
    @InjectMocks
    private AuthorizedUserResolver resolver;

    @Test
    @DisplayName("supportsParameter 메서드는 SignUser 어노테이션이 붙은 String Type 지원")
    void supportsParameter() {
        MethodParameter parameter = mock(MethodParameter.class);

        doReturn(true).when(parameter).hasParameterAnnotation(SignUser.class);
        doReturn(AuthUserRequest.class).when(parameter).getParameterType();

        boolean result = resolver.supportsParameter(parameter);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("String Type이 아닌 경우, false 반환")
    void notSupportsParameter() {
        MethodParameter parameter = mock(MethodParameter.class);

        doReturn(true).when(parameter).hasParameterAnnotation(SignUser.class);
        doReturn(Integer.class).when(parameter).getParameterType();

        boolean result = resolver.supportsParameter(parameter);

        assertThat(result).isFalse();
    }
}
