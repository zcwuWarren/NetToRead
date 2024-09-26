package com.personal_project.Next_to_read.util.EntityToDtoConverter;

import com.personal_project.Next_to_read.data.dto.BookInfoDto;
import com.personal_project.Next_to_read.model.BookInfo;

import java.util.List;
import java.util.stream.Collectors;

public class BookInfoDtoConverter {

    public static BookInfoDto convertToDto(BookInfo bookInfo) {
        return BookInfoDto.builder()
                .bookId(bookInfo.getBookId())
                .isbn(bookInfo.getIsbn())
                .mainCategory(bookInfo.getMainCategory())
                .subCategory(bookInfo.getSubCategory())
                .bookName(bookInfo.getBookName())
                .bookCover(bookInfo.getBookCover())
                .publishDate(bookInfo.getPublishDate())
                .publisher(bookInfo.getPublisher())
                .like(bookInfo.getLikes())
                .collect(bookInfo.getCollect())
                .content(bookInfo.getContent())
                .description(bookInfo.getDescription())
                .author(bookInfo.getAuthor())
                .build();
    }

    public static List<BookInfoDto> convertToDtoList(List<BookInfo> bookInfos) {
        return bookInfos.stream()
                .map(BookInfoDtoConverter::convertToDto)
                .collect(Collectors.toList());
    }
}

