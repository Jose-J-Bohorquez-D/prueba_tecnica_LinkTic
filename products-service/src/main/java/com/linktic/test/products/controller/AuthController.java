//archivo: products-service/src/main/java/com/linktic/test/products/controller/AuthController.java
package com.linktic.test.products.controller;

import com.linktic.test.products.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {


    private final JwtService jwtService;

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> body) {

        String username = body.get("username");
        String password = body.get("password");

        if ("admin".equals(username) && "admin".equals(password)) {
            String token = jwtService.generateToken(username);
            return Map.of("token", token);
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
    }
}
