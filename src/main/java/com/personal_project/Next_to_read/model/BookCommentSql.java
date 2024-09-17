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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false)
    private String bookId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "comment")
    private String comment;

    @ElementCollection
    @CollectionTable(name = "book_highlights", joinColumns = @JoinColumn(name = "book_comment_id"))
    @Column(name = "highlight")
    private List<String> highlights;
}
