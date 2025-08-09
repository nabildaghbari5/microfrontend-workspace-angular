package com.talentcloud.job.config;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null) {
                    template.header("Authorization", authHeader);
                    log.debug("🔑 Added Authorization header to Feign request");
                }
            }
        };
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(10000, 10000);
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    public static class CustomErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultErrorDecoder = new Default();

        @Override
        public Exception decode(String methodKey, feign.Response response) {
            log.error("🔴 Feign Error - Method: {}, Status: {}, Reason: {}",
                    methodKey, response.status(), response.reason());

            // Log the response body if available
            try {
                if (response.body() != null) {
                    String responseBody = new String(response.body().asInputStream().readAllBytes());
                    log.error("🔴 Response Body: {}", responseBody);
                }
            } catch (Exception e) {
                log.error("🔴 Could not read response body", e);
            }

            if (response.status() >= 400 && response.status() <= 499) {
                log.error("🔴 Client Error (4xx) - Check request format/authentication");
            } else if (response.status() >= 500) {
                log.error("🔴 Server Error (5xx) - Target service is down or has issues");
            }

            return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}