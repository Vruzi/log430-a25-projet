package com.log430.brokerx.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import com.log430.brokerx.dto.CreateUserRequest;
import com.log430.brokerx.model.User;
import com.log430.brokerx.repository.UserRepository;

@RestController
@RequestMapping("/api")
public class ExempleBDController {

    private final UserRepository userRepository;

    public ExempleBDController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/user")
    public User createUser(@RequestBody CreateUserRequest req) {
        String hash = "{noop}" + req.password;
        User user = new User(req.username, req.email, hash);
        return userRepository.save(user);
    }
}
