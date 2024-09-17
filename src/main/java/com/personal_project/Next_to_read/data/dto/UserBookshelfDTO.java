package com.personal_project.Next_to_read.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBookshelfDTO {
    private String userId;

    private List<String> likedBooks;

    private List<String> collectedBooks;

    // 用來記錄用戶對書籍的評論
    private List<String> commentedBooks;

    // 用來記錄用戶對書籍的畫線
    private List<String> highlightedBooks;
}
