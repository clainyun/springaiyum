package com.ssafy.yumyum.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CommunityPost {

    private String id;
    private String userId;
    private String category;
    private String linkedMealId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
