package com.personal_project.Next_to_read.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private String userId;
    private String bookId;
    private List<String> comment;
}
