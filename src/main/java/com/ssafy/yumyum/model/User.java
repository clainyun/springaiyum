package com.ssafy.yumyum.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class User {

    private String id;
    private String email;
    private String password;
    private String nickname;
    private String gender;
    private int birthYear;
    private double height;
    private double weight;
    private String goal;
    private String healthNote;
    private boolean active;
    private LocalDateTime createdAt;
}
