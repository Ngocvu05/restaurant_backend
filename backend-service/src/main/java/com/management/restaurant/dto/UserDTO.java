package com.management.restaurant.dto;

import com.management.restaurant.common.RoleName;
import com.management.restaurant.common.UserStatus;
import com.management.restaurant.model.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone_number;
    private String address;
    private RoleName roleType;
    private String password;
    private UserStatus status;
    private LocalDateTime createdAt;
    @Builder.Default
    private List<ImageDTO> images = new ArrayList<>();
}
