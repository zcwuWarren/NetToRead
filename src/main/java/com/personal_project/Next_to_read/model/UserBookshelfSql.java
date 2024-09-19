package com.personal_project.Next_to_read.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//import javax.persistence.*;


@Entity
@Table(name = "user_bookshelf")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBookshelfSql {

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

    @Column(name = "likes", nullable = false)
    private Boolean likes;

    @Column(name = "collect", nullable = false)
    private Boolean collect;
}