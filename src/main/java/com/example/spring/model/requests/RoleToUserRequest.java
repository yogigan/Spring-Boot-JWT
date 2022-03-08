package com.example.spring.model.requests;

import com.example.spring.model.domain.Role;
import lombok.Data;

/**
 * @author Yogi
 * @since 08/02/2022
 */
@Data
public class RoleToUserRequest {
    private String username;
    private Role roleName;
}
