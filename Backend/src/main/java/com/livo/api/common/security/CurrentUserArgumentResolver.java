package com.livo.api.common.security;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

/**
 * Resolves @CurrentUser annotated parameters in Spring MVC controller handler methods.
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class) &&
                (UserPrincipal.class.isAssignableFrom(parameter.getParameterType()) ||
                 UUID.class.isAssignableFrom(parameter.getParameterType()));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        if (UUID.class.isAssignableFrom(parameter.getParameterType())) {
            return SecurityUtils.getCurrentUserId();
        }
        return SecurityUtils.getCurrentUserPrincipal().orElse(null);
    }
}
