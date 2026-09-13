package com.tokenization.mask.controller;

import com.tokenization.mask.dto.UserPayload;
import com.tokenization.mask.security.TokenBody;
import com.tokenization.mask.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The actual "tokenized" resource: {@code create} takes no JSON body at all - the
 * request payload arrives entirely inside the caller's {@code Authorization: Bearer}
 * JWT, decoded by {@link TokenBody}.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserPayload> create(@TokenBody UserPayload payload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(payload));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserPayload> get(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }
}
