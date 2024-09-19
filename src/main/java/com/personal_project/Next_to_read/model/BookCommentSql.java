package com.personal_project.Next_to_read.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "book_comment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookCommentSql {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "book_id", nullable = false)
//    private String bookId;
//
//    @Column(name = "user_id", nullable = false)
//    private String userId;
//
//    @ElementCollection
//    @Column(name = "comment")
//    private List<String> comment;
//
//    @ElementCollection
//    @CollectionTable(name = "book_highlights", joinColumns = @JoinColumn(name = "book_comment_id"))
//    @Column(name = "highlight")
//    private List<String> highlights;
//}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-One relation with User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Many-to-One relation with BookInfo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private BookInfo book;

    @Column(name = "comment", nullable = true)
    private String comment;

    @Column(name = "quote", nullable = true)
    private String quote;
}
