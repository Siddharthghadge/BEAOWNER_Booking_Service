package com.carrental.bookingservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String header = request.getHeader("Authorization");
                if (header != null) {
                    template.header("Authorization", header);
                }
            } else {
                // 💡 LOG THIS: If this prints, it means the thread lost the security context
                System.out.println("⚠️ FEIGN INTERCEPTOR: No RequestContext found!");
            }
        };

    }
}