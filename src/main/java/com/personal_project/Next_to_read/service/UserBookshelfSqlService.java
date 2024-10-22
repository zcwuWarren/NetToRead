package com.personal_project.Next_to_read.service;

import com.personal_project.Next_to_read.data.dto.BookInfoDto;
import com.personal_project.Next_to_read.data.dto.UserBookshelfDto;
import com.personal_project.Next_to_read.jwt.JwtTokenUtil;
import com.personal_project.Next_to_read.model.BookInfo;
import com.personal_project.Next_to_read.model.User;
import com.personal_project.Next_to_read.model.UserBookshelfSql;
import com.personal_project.Next_to_read.repository.UserBookshelfSqlRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserBookshelfSqlService {

    private final UserBookshelfSqlRepository userBookshelfSqlRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public UserBookshelfSqlService(UserBookshelfSqlRepository userBookshelfSqlRepository, JwtTokenUtil jwtTokenUtil) {
        this.userBookshelfSqlRepository = userBookshelfSqlRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    public List<UserBookshelfDto> getLikeByUserId(String token, int offset, int limit) {

        User user = jwtTokenUtil.getUserFromToken(token);
        Long userId = user.getUserId();

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<UserBookshelfSql> likesPage = userBookshelfSqlRepository.findLikedBooksByUserId(userId, pageable);

        return likesPage.getContent().stream()
                .map(UserBookshelfDto::new)
                .collect(Collectors.toList());
    }

    public List<UserBookshelfDto> getCollectByUserId(String token, int offset, int limit) {

        User user = jwtTokenUtil.getUserFromToken(token);
        Long userId = user.getUserId();

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<UserBookshelfSql> likesPage = userBookshelfSqlRepository.findCollectedBooksByUserId(userId, pageable);

        return likesPage.getContent().stream()
                .map(UserBookshelfDto::new)
                .collect(Collectors.toList());
    }

    public boolean isBookLikedByUser(Long bookId, String token) {
        User user = jwtTokenUtil.getUserFromToken(token);
        Long userId = user.getUserId();

        Boolean likeStatus = userBookshelfSqlRepository.findLikeStatusByUserIdAndBookId(userId, bookId);
        return likeStatus != null && likeStatus;
    }

    public boolean isBookCollectedByUser(Long bookId, String token) {
        User user = jwtTokenUtil.getUserFromToken(token);
        Long userId = user.getUserId();

        Boolean collectStatus = userBookshelfSqlRepository.findCollectStatusByUserIdAndBookId(userId, bookId);
        return collectStatus != null && collectStatus;
    }
}

