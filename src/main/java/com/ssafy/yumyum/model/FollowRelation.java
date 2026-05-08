package com.ssafy.yumyum.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FollowRelation {

    private String id;
    private String followerId;
    private String followeeId;
    private LocalDateTime createdAt;
}
