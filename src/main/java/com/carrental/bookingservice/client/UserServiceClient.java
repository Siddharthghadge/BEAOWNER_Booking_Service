package com.carrental.bookingservice.client;

import com.carrental.bookingservice.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "userservice", url = "${USER_SERVICE_URL}")
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    UserResponse getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/users/email/{email}")
    UserResponse getUserByEmail(@PathVariable String email);
}
