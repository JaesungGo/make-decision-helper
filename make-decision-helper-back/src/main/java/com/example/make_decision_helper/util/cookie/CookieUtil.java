package com.example.make_decision_helper.util.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.*;

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

    public static ResponseCookie deleteGuestCookie(){
        ResponseCookie responseCookie = ResponseCookie.from("guestToken","")
                .maxAge(0)
                .path("/")
                .build();
        return responseCookie;
    }


    /**
     * 웹소켓 헤더이서 쿠키 추출하는 클래스
     * @param headers
     * @param cookieName
     * @return
     */
    public static String extractTokenFromWebSocketCookie(Map<String, List<String>> headers, String cookieName) {
        List<String> cookies = headers.get("cookie");  // STOMP에서는 소문자 "cookie"로 전달됨
        if (cookies == null || cookies.isEmpty()) return null;

        // STOMP 헤더는 List 형태로 전달되므로, 첫 번째 요소를 사용
        String cookieString = cookies.get(0);

        // 쿠키 문자열 파싱
        String[] cookiePairs = cookieString.split("; ");
        for (String cookiePair : cookiePairs) {
            String[] keyValue = cookiePair.split("=");
            if (keyValue.length == 2 && cookieName.equals(keyValue[0])) {
                return keyValue[1];
            }
        }
        return null;
    }

//    /**
//     * 서블릿 request를 HttpHeaders객체로 사용
//     * @param request
//     * @param httpHeaders
//     */
//    public static void servletRequestToHttpHeaders(HttpServletRequest request, HttpHeaders httpHeaders) {
//        Enumeration<String> headerNames = request.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            String headerValue = request.getHeader(headerName);
//            httpHeaders.add(headerName, headerValue);
//        }
//    }


}
