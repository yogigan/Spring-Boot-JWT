package com.example.spring.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @author Yogi
 * @since 15/02/2022 -
 */
@Data
@Builder
@AllArgsConstructor
public class TokenResponse {
    private String value;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss", timezone = "Asia/Jakarta")
    private Date expiredAt;
}
