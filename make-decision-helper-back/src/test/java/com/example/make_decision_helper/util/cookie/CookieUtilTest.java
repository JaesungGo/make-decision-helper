package com.example.make_decision_helper.util.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("CookieUtil 관련 테스트")
class CookieUtilTest {


    @Test
    @DisplayName("쿠키 생성 테스트")
    void createCookie() {

        //given
        String name = "testCookie";
        String value = "testValue";
        Long maxAge = 3600L;


        //when
        ResponseCookie createdCookie = CookieUtil.createCookie(name, value, maxAge);

        //then
        assertAll(
                ()-> assertEquals(createdCookie.getName(), name),
                ()-> assertEquals(createdCookie.getValue(), value),
                ()-> assertEquals(createdCookie.getMaxAge().getSeconds(), maxAge),
                ()-> assertTrue(createdCookie.isSecure()),
                ()-> assertTrue(createdCookie.isHttpOnly())
        );

    }

    @Test
    @DisplayName("쿠키 값 추출 테스트")
    void getCookieValue() {

        //given
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        Cookie testCookie = new Cookie("testCookie", "testValue");
        HttpHeaders httpHeaders = new HttpHeaders();
//      Cookie[] cookies = new Cookie[]{testCookie};
        when(httpServletRequest.getCookies()).thenReturn(new Cookie[]{testCookie});

        //when
        String testCookieValue = CookieUtil.getCookieValue(httpServletRequest, "testCookie");

        //then
        assertEquals("testValue", testCookieValue);

    }

    /**
     * ResponseCookie의 MaxAge는 Duration 타입이므로 getSeconds()를 통해 Long으로 맞춰줘야 함
     */
    @Test
    @DisplayName("로그아웃용 쿠키 생성 테스트")
    void logoutCookie() {

        //given
        
        //when
        Map<String, ResponseCookie> deleteCookies = CookieUtil.createLogoutCookie();

        //then
        assertAll(
                () -> assertTrue(deleteCookies.containsKey("accessToken")),
                () -> assertTrue(deleteCookies.containsKey("refreshToken")),
                () -> assertEquals(0L,deleteCookies.get("accessToken").getMaxAge().getSeconds()),
                () -> assertEquals(0L,deleteCookies.get("refreshToken").getMaxAge().getSeconds())
        );

    }
}