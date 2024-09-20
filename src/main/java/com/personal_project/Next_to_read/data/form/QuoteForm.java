package com.personal_project.Next_to_read.data.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuoteForm {

    @NotBlank
    private String quote;

    private String token;
}
