package com.personal_project.Next_to_read.repository;

import com.personal_project.Next_to_read.model.UserBookshelf;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

public interface UserBookshelfRepository extends MongoRepository<UserBookshelf, String> {
    @Query(value = "{ 'userId': ?0 }")
    @Update(value = "{ '$addToSet': { 'commentedBooks': ?1 } }")  // 保證不重複添加
    void addCommentedBook(String userId, String bookId);

    @Query(value = "{ 'userId': ?0 }")
    @Update(value = "{ '$addToSet': { 'highlightedBooks': ?1 } }")  // 保證不重複添加
    void addHighlightedBook(String userId, String bookId);
}
