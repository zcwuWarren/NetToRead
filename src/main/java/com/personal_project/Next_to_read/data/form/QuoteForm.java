package com.personal_project.Next_to_read.data.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuoteForm {

    @NotBlank
    private String Quote;

    private String token;
}
