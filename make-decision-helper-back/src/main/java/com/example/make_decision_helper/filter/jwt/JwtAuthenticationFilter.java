package com.example.make_decision_helper.filter.jwt;

import com.example.make_decision_helper.util.cookie.CookieUtil;
import com.example.make_decision_helper.util.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String accessToken = CookieUtil.getCookieValue(request, "accessToken");

        if (StringUtils.hasText(accessToken)) {
            try {
                if (jwtTokenProvider.validateToken(accessToken)) {
                    // 토큰이 유효한 경우
                    Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // 토큰이 만료된 경우
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("토큰이 만료됨");
                    return;
                }
            } catch (Exception e) {
                log.error("Security Context 설정 중 에러 발생: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }


}