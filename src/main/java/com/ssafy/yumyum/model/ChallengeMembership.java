package com.ssafy.yumyum.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ChallengeMembership {

    private String id;
    private String challengeId;
    private String userId;
    private int progress;
    private String status;
    private LocalDateTime joinedAt;
    private LocalDateTime updatedAt;
}
