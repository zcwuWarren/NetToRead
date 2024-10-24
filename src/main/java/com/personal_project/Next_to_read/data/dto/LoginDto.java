package com.personal_project.Next_to_read.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.personal_project.Next_to_read.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginDto {

    @JsonProperty("token")
    private String accessToken;

    @JsonProperty("expired")
    private long accessExpired;
}
