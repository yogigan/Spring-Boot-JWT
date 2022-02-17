package com.example.spring.model.requests;

import lombok.Data;

/**
 * @author Yogi
 * @since 08/02/2022
 */
@Data
public class RoleToUserRequest {
    private String username;
    private String roleName;
}
