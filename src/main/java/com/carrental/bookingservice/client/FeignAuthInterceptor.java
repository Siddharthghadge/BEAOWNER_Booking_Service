package com.carrental.bookingservice.client;

import com.carrental.bookingservice.config.JwtContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignAuthInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {

        String token = JwtContextHolder.getToken();

        if (token != null) {
            template.header("Authorization", "Bearer " + token);
        }
    }
}
