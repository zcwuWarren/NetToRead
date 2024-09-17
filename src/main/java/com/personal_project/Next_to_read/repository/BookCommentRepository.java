//package com.personal_project.Next_to_read.repository;
//
//import com.personal_project.Next_to_read.model.BookComment;
//import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.data.mongodb.repository.Query;
//import org.springframework.data.mongodb.repository.Update;
//
//public interface BookCommentRepository extends MongoRepository<BookComment, String> {
//
//    @Query(value = "{ 'bookId': ?0, 'userComments.userId': ?1 }")
//    @Update(value = "{ '$set': { 'userComments.$.comment': ?2 } }")
//    void addOrUpdateComment(String bookId, String userId, String comment);
//
//    @Query(value = "{ 'bookId': ?0, 'userComments.userId': ?1 }")
//    @Update(value = "{ '$push': { 'userComments.$.highlight': ?2 } }")
//    void addHighlight(String bookId, String userId, String highlight);
//
//    @Query(value = "{ 'bookId': ?0, 'userComments.userId': ?1, 'userComments.$.highlight': ?2 }")
//    @Update(value = "{ '$set': { 'userComments.$.highlight.$': ?3 } }")
//    void updateHighlight(String bookId, String userId, String oldHighlight, String newHighlight);
//
//    @Query(value = "{ 'bookId': ?0, 'userComments.userId': ?1 }")
//    @Update(value = "{ '$unset': { 'userComments.$.comment': '' } }")
//    void deleteComment(String bookId, String userId);
//
//    @Query(value = "{ 'bookId': ?0, 'userComments.userId': ?1 }")
//    @Update(value = "{ '$pull': { 'userComments.$.highlight': ?2 } }")
//    void deleteHighlight(String bookId, String userId, String highlight);
//}
