package com.example.spring.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

import java.util.Date;

/**
 * @author Yogi
 * @since 06/02/2022
 */
@Data
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {

    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss", timezone = "Asia/Jakarta")
    private Date timestamp = new Date();
    private Integer code;
    private HttpStatus status;
    private String message;
    private Object data;
    private String reason;

}
