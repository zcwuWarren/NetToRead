//package com.personal_project.Next_to_read.model;
//
//import jakarta.persistence.Id;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//import java.util.List;
//
//@Document(collection = "book_comment")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class BookComment {
//
//    @Id
//    private String bookId;  // MongoDB 中的主鍵
//
//    private List<UserComment> userComments;  // 每個使用者的評論與標記
//
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class UserComment {
//
//        private String userId;
//
//        private String comment;
//
//        private String highlight;
//    }
//}
