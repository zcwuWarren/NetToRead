package com.personal_project.Next_to_read.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "user_bookshelf")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBookshelf {

    @Id
    private String userId;  // MongoDB 中的主鍵

    private List<String> likes;  // 儲存書籍的 book_id

    private List<String> collects;  // 儲存書籍的 book_id
}
