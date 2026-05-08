package com.ssafy.yumyum.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CommunityComment {

    private String id;
    private String postId;
    private String userId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
