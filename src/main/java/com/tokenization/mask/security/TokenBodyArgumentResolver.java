package com.tokenization.mask.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves {@code @TokenBody} parameters by re-decoding the raw bearer token that
 * {@link JwtAuthenticationFilter} already validated and stashed on the request,
 * converting its claims into the controller method's declared parameter type.
 */
public class TokenBodyArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String RAW_TOKEN_ATTRIBUTE = "tokenized.rawToken";

    private final JwtService jwtService;

    public TokenBodyArgumentResolver(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(TokenBody.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                   NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        Object rawToken = request != null ? request.getAttribute(RAW_TOKEN_ATTRIBUTE) : null;
        if (!(rawToken instanceof String token) || token.isBlank()) {
            throw new JwtException("No bearer token carrying a request payload was found on this request");
        }
        return jwtService.parse(token, parameter.getParameterType());
    }
}
