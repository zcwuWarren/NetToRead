package com.personal_project.Next_to_read.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HighlightDto {
    private String userId;
    private String bookId;
    // add or delete highlights
    private List<String> highlights;
    // 用於 updateHighlight 時傳遞舊的 highlight
    private String oldHighlight;
    // 用於 updateHighlight 時傳遞新的 highlight
    private String newHighlight;


}

