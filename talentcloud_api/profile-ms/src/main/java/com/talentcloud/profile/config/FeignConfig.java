package com.talentcloud.profile.config;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class FeignConfig {

//    @Bean
//    public RequestInterceptor requestInterceptor() {
//        return template -> {
//            log.debug("🔧 FeignConfig - Processing request to: {}", template.url());
//
//            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//            if (attrs != null) {
//                HttpServletRequest request = attrs.getRequest();
//
//                // Check for Authorization header
//                String authHeader = request.getHeader("Authorization");
//                if (authHeader != null) {
//                    template.header("Authorization", authHeader);
//                    log.debug("🔑 Added Authorization header: {}", authHeader.substring(0, Math.min(20, authHeader.length())) + "...");
//                } else {
//                    log.warn("⚠️ No Authorization header found in request");
//                }
//
//                // Check for other important headers
//                String userIdHeader = request.getHeader("X-User-Id");
//                String rolesHeader = request.getHeader("X-User-Roles");
//
//                log.debug("📋 Request headers - X-User-Id: {}, X-User-Roles: {}", userIdHeader, rolesHeader);
//
//                // Log all headers for debugging
//                log.debug("🔍 All request headers:");
//                request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
//                    String headerValue = request.getHeader(headerName);
//                    log.debug("   {}: {}", headerName, headerValue);
//                });
//
//            } else {
//                log.warn("⚠️ No request attributes found - this might be the issue!");
//            }
//        };
//    }
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            log.debug("Processing request to: {}", template.url());
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                // Ne pas ajouter l'en-tête Authorization
                // String authHeader = request.getHeader("Authorization");
                // if (authHeader != null) {
                //     template.header("Authorization", authHeader);
                // }
                log.debug("Headers added to Feign request (Authorization excluded)");
            }
        };
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // Log everything for debugging
    }

    @Bean
    public Request.Options options() {
        return new Request.Options(5000, TimeUnit.MILLISECONDS, 5000, TimeUnit.MILLISECONDS, false);
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(1000, 2000, 3);
    }
}