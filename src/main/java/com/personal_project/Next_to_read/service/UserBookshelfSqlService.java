package com.personal_project.Next_to_read.service;

import com.personal_project.Next_to_read.data.dto.BookInfoDto;
import com.personal_project.Next_to_read.data.dto.UserBookshelfDto;
import com.personal_project.Next_to_read.jwt.JwtTokenUtil;
import com.personal_project.Next_to_read.model.BookInfo;
import com.personal_project.Next_to_read.model.User;
import com.personal_project.Next_to_read.model.UserBookshelfSql;
import com.personal_project.Next_to_read.repository.UserBookshelfSqlRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserBookshelfSqlService {

    private final UserBookshelfSqlRepository userBookshelfSqlRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public UserBookshelfSqlService(UserBookshelfSqlRepository userBookshelfSqlRepository, JwtTokenUtil jwtTokenUtil) {
        this.userBookshelfSqlRepository = userBookshelfSqlRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    public List<UserBookshelfDto> getCollectByUserId(String token) {

        User user = jwtTokenUtil.getUserFromToken(token);
        Long userId = user.getUserId();

        List<UserBookshelfSql> collects = userBookshelfSqlRepository.findByUserId_UserIdAndCollectTrueOrderByTimestampCollectDesc(userId);

        // 刪除功能還沒完成
//        collects.forEach(userBookshelf -> {
//            if (!userBookshelf.getLikes() && !userBookshelf.getCollect()) {
//                // if likes and collect are both false, delete the record
//                userBookshelfSqlRepository.delete(userBookshelf);
//            }
//        });

        // turn result to BookCollectDto
        return collects.stream().map(collect -> new UserBookshelfDto(collect)).collect(Collectors.toList());
    }

    public List<UserBookshelfDto> getLikeByUserId(String token) {

        User user = jwtTokenUtil.getUserFromToken(token);
        Long userId = user.getUserId();

        List<UserBookshelfSql> likes = userBookshelfSqlRepository.findByUserId_UserIdAndLikesTrueOrderByTimestampLikeDesc(userId);

        // turn result to BookCollectDto
        return likes.stream().map(like -> new UserBookshelfDto(like)).collect(Collectors.toList());
    }
}

