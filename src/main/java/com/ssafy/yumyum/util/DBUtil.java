package com.ssafy.yumyum.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DBUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/ssafy_yumyumcoach?characterEncoding=UTF-8";
    private static final String USER = "ssafy";
    private static final String PASSWORD = "ssafy";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL Driver 로딩 실패: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
