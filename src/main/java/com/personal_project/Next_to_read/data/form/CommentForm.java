package com.personal_project.Next_to_read.data.form;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CommentForm {

    @NotEmpty
    private String Comment;

    private String token;
}
