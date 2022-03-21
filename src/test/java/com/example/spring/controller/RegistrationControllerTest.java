package com.example.spring.controller;

import com.example.spring.model.requests.RegisterRequest;
import com.example.spring.model.response.ApiResponse;
import com.example.spring.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(RegistrationController.class)
class RegistrationControllerTest {

    @MockBean
    private RegistrationService registrationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testRegister() throws Exception {
        //given
        String token = UUID.randomUUID().toString();
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("john")
                .lastName("doe")
                .username("johndoe")
                .email("mail@gmail.com")
                .password("toor")
                .build();

        //when
        when(registrationService.register(registerRequest)).thenReturn(token);

        //then
        ApiResponse response = ApiResponse.ok("User registered successfully",
                Collections.singletonMap("confirmationToken", token));
        mockMvc.perform(post("/")
                        .contentType("application/json")
                        .content(response)
                .andExpect(status().isOk());

    }

    @Test
    void testConfirm() {
        //given

        //when

        //then
    }
}