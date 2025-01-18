package com.example.make_decision_helper.util.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CookieUtil {

    /**
     * 쿠키 생성
     * @param name 쿠키 이름
     * @param value 쿠키 값
     * @param maxAge 쿠키 만료 시간 (초단위)
     * @return 생성된 쿠키 객체
     */
    public static ResponseCookie createCookie(String name, String value, Long maxAge){
        return ResponseCookie.from(name,value)
                .path("/")
                .secure(true)
                .sameSite("Strict")
                .httpOnly(true)
                .maxAge(maxAge)
                .build();
    }

    /**
     * 쿠키에서 값 추출
     * @param request HTTP 요청 객체
     * @param name 쿠키 이름
     * @return 쿠키 값, 없으면 null
     */
    public static String getCookieValue(HttpServletRequest request, String name){
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(cookie.getName().equals(name)){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 로그아웃용 쿠키 생성
     * @param
     * @return 기간 만료된 쿠키 객체 Map<String, ResponseCookie></>
     */
    public static Map<String, ResponseCookie> createLogoutCookie(){
        Map<String,ResponseCookie> cookies = new HashMap<>();
        cookies.put("accessToken",createCookie("accessToken","",0L));
        cookies.put("refreshToken",createCookie("refreshToken","",0L));
        return cookies;
    }
}
