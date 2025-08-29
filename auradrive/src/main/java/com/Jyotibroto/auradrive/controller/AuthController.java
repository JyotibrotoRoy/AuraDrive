package com.Jyotibroto.auradrive.controller;

import com.Jyotibroto.auradrive.entity.User;
import com.Jyotibroto.auradrive.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private static AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user){
        authService.registerNewUser(user);
        return ResponseEntity.ok("User Registered succesfully");
    }
}
