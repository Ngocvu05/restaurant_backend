package com.management.restaurant.controller;

import com.management.restaurant.dto.UserDTO;
import com.management.restaurant.exception.NotFoundException;
import com.management.restaurant.model.User;
import com.management.restaurant.security.UserPrincipal;
import com.management.restaurant.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/by-username/{username}")
    public ResponseEntity<UserDTO> getByUserName(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDTO));
    }

    @PutMapping("/update")
    public ResponseEntity<UserDTO> updateUser(@AuthenticationPrincipal UserPrincipal principal, @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(principal.getId(), userDTO));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getUserProfile(@AuthenticationPrincipal UserPrincipal principal, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        System.out.println(">>> Token received in user-service: " + token);
        UserDTO dto = userService.getUserById(principal.getId());
        if (dto == null) {
            throw new NotFoundException("User not found");
        }
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/avatar/{imageId}")
    public ResponseEntity<String> setAvatar(@PathVariable Long imageId, @AuthenticationPrincipal UserPrincipal principal) throws BadRequestException {
        userService.setAvatarImage(principal.getId(), imageId);
        return ResponseEntity.ok("Đã cập nhật avatar thành công");
    }

    @DeleteMapping("/delete{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
