package com.personal_project.Next_to_read.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "book_comment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookCommentSql {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    // Many-to-One relation with User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

    // Many-to-One relation with BookInfo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private BookInfo bookId;

    @Column(name = "comment")
    private String comment;

//    @Column(name = "quote")
//    private String quote;
}
