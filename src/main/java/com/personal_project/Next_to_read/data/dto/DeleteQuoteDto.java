package com.personal_project.Next_to_read.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteQuoteDto {
    private Long id;
    private String token;
}
