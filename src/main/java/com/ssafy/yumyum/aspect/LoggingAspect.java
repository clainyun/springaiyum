package com.ssafy.yumyum.aspect;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around(
        "execution(* com.ssafy.yumyum.service..*(..)) || "
            + "execution(* com.ssafy.yumyum.repository..*(..))"
    )
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        String signature = joinPoint.getSignature().toShortString();
        String argTypes = Arrays.stream(joinPoint.getArgs())
            .map(arg -> arg == null ? "null" : arg.getClass().getSimpleName())
            .collect(Collectors.joining(", "));

        log.debug("Enter {} args=[{}]", signature, argTypes);

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.debug("Exit {} ({} ms)", signature, elapsed);
            return result;
        } catch (Throwable throwable) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("Error {} ({} ms)", signature, elapsed, throwable);
            throw throwable;
        }
    }
}
