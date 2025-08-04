package com.management.restaurant.admin.controller;

import com.management.restaurant.admin.service.AdminUserService;
import com.management.restaurant.admin.service.NotificationService;
import com.management.restaurant.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/users/")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminUserService adminUserService;
    private final NotificationService notificationService;

    @GetMapping("/")
    public List<UserDTO> getAllUsers() {
        return adminUserService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDTO getById(@PathVariable Long id) {
        return adminUserService.getById(id);
    }

    @PostMapping("/")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO dto) {
        adminUserService.create(dto);
        notificationService.notifyAllAdmins("Thêm tài khoản mới", "Admin đã thêm món mới: " + dto.getUsername());
        return ResponseEntity.ok().body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO dto) {
        adminUserService.update(id, dto);
        return ResponseEntity.ok().body(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        adminUserService.delete(id);
        return  ResponseEntity.ok("Deleted");
    }
}
