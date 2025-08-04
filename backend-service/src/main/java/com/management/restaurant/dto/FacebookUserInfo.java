package com.management.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacebookUserInfo {
    private String id;
    private String name;
    private String email;
    private String firstName;
    private String lastName;
    private String pictureUrl;
    private boolean verified;
}
