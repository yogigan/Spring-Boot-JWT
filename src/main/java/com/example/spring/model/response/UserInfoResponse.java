package com.example.spring.model.response;

import com.example.spring.model.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author Yogi
 * @since 17/02/2022
 */
@Data
@Builder
@AllArgsConstructor
public class UserInfoResponse {

    private String firstName;
    private String lastName;
    private String email;
    private String userName;
    private List<Role> roles;
}
