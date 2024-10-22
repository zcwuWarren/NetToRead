package com.personal_project.Next_to_read.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "quote")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quote {

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

    @Column(name = "quote", columnDefinition = "TEXT", nullable = false)
    private String quote;

    @Column(name = "timestamp", nullable = false)
    private Timestamp timestamp;

    @Column(name = "main_category", nullable = false)
    private String mainCategory;

    @Column(name = "sub_category", nullable = false)
    private String subCategory;
}
