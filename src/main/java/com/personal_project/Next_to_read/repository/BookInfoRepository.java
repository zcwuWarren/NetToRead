package com.personal_project.Next_to_read.repository;

import com.personal_project.Next_to_read.model.BookInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

//import java.awt.print.Pageable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface BookInfoRepository extends JpaRepository<BookInfo, Long> {

    Optional<BookInfo> findByBookId(Long bookId);

    @Query("SELECT DISTINCT b.mainCategory AS mainCategory, b.subCategory AS subCategory FROM BookInfo b")
    List<Map<String, Object>> findDistinctCategories();

    List<BookInfo> findTop6BySubCategoryOrderByLikesDesc(String subCategory);

    List<BookInfo> findTop6ByOrderByLikesDesc();

    List<BookInfo> findTop6BySubCategoryOrderByCollectDesc(String subCategory);

    List<BookInfo> findTop6ByBookIdIn(List<Long> bookIds);

    List<BookInfo> findByBookIdIn(List<Long> bookIds);

    List<BookInfo> findByBookNameContaining(String keyword);

    Page<BookInfo> findByBookNameContainingIgnoreCase(String keyword, Pageable pageable);

}
