package com.example.make_decision_helper.config.logging;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private final ObjectMapper objectMapper;

    public LoggingAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Pointcut("execution(* com.example.make_decision_helper.controller..*.*(..))")
    private void controllerPointcut() {}

    @Pointcut("execution(* com.example.make_decision_helper.service..*.*(..))")
    private void servicePointcut() {}

    @Around("controllerPointcut() || servicePointcut()")
    public Object loggingAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        // 파라미터 로깅 처리를 위한 메서드 추출
        String params = getParametersAsString(joinPoint.getArgs());

        // 메서드 실행 전 로깅
        log.info("[{}] {}: --> Parameters: {}",
                className,
                methodName,
                params
        );

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            String resultStr = getResultAsString(result);

            long endTime = System.currentTimeMillis();
            log.info("[{}] {}: <-- Response: {} ({}ms)",
                    className,
                    methodName,
                    resultStr,
                    endTime - startTime
            );

            return result;

        } catch (Exception e) {
            // 예외 발생 시 로깅
            log.error("[{}] {}: XX Exception: {}",
                    className,
                    methodName,
                    e.getMessage()
            );
            throw e;
        }
    }

    /**
     * 메서드 파라미터들을 문자열로 변환
     * HttpServletRequest, HttpServletResponse 등 직렬화가 불가능한 객체들을 적절히 처리
     */
    private String getParametersAsString(Object[] args) {
        try {
            if (args == null || args.length == 0) {
                return "[]";
            }

            return Stream.of(args)
                    .map(this::convertToString)
                    .reduce((a, b) -> a + ", " + b)
                    .map(s -> "[" + s + "]")
                    .orElse("[]");
        } catch (Exception e) {
            log.warn("Failed to convert parameters to string: {}", e.getMessage());
            return "[Failed to serialize parameters]";
        }
    }

    /**
     * 결과값을 문자열로 변환
     * 직렬화가 불가능한 객체들을 적절히 처리
     */
    private String getResultAsString(Object result) {
        try {
            if (result == null) {
                return "null";
            }
            return convertToString(result);
        } catch (Exception e) {
            log.warn("Failed to convert result to string: {}", e.getMessage());
            return "[Failed to serialize response]";
        }
    }

    /**
     * 개별 객체를 문자열로 변환
     * 특수한 타입들에 대한 처리 로직 포함
     */
    private String convertToString(Object obj) {
        try {
            if (obj == null) {
                return "null";
            }

            // HttpServletRequest/Response는 타입만 로깅
            if (obj instanceof HttpServletRequest) {
                return "HttpServletRequest";
            }
            if (obj instanceof HttpServletResponse) {
                return "HttpServletResponse";
            }

            // 나머지 객체는 JSON으로 직렬화
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.getClass().getSimpleName() + "@" + System.identityHashCode(obj);
        }
    }
}