package com.personal_project.Next_to_read.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "user_bookshelf")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBookshelfSql {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "user_id", nullable = false)
//    private String userId;
//
//    @ElementCollection
//    @CollectionTable(name = "user_likes", joinColumns = @JoinColumn(name = "user_bookshelf_id"))
//    @Column(name = "book_id")
//    private List<String> likedBooks;
//
//    @ElementCollection
//    @CollectionTable(name = "user_collects", joinColumns = @JoinColumn(name = "user_bookshelf_id"))
//    @Column(name = "book_id")
//    private List<String> collectedBooks;
//
//    @ElementCollection
//    @CollectionTable(name = "user_comments", joinColumns = @JoinColumn(name = "user_bookshelf_id"))
//    @Column(name = "book_id")
//    private List<String> commentedBooks;
//
//    @ElementCollection
//    @CollectionTable(name = "user_highlights", joinColumns = @JoinColumn(name = "user_bookshelf_id"))
//    @Column(name = "book_id")
//    private List<String> highlightedBooks;
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

    @Column(name = "likes", nullable = false)
    private Boolean likes;

    @Column(name = "collect", nullable = false)
    private Boolean collect;
}