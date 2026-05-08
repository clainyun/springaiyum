package com.ssafy.yumyum.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.util.DBUtil;

public class UserRepository {

    private static final String CREATE_USERS_SQL = """
        CREATE TABLE IF NOT EXISTS users (
            user_id VARCHAR(64) NOT NULL PRIMARY KEY,
            email VARCHAR(100) NOT NULL,
            password VARCHAR(100) NOT NULL,
            nickname VARCHAR(50) NOT NULL,
            gender VARCHAR(20) NULL,
            birth_year INT NOT NULL,
            height DOUBLE NOT NULL,
            weight DOUBLE NOT NULL,
            goal VARCHAR(100) NULL,
            health_note VARCHAR(500) NULL,
            active BOOLEAN NOT NULL DEFAULT TRUE,
            created_at DATETIME NOT NULL,
            updated_at DATETIME NOT NULL,
            UNIQUE KEY uq_users_email (email)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """;

    public UserRepository(List<User> seedUsers) {
        initializeSchema();
        seedIfEmpty(seedUsers == null ? List.of() : seedUsers);
    }

    public UserRepository() {
        initializeSchema();
    }

    public synchronized List<User> findAll() {
        String sql = """
            SELECT user_id, email, password, nickname, gender, birth_year, height, weight,
                   goal, health_note, active, created_at
              FROM users
             ORDER BY created_at ASC, user_id ASC
            """;

        List<User> users = new ArrayList<>();
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("사용자 목록을 조회하지 못했습니다.", e);
        }
        return users;
    }

    public synchronized User findById(String id) {
        String sql = """
            SELECT user_id, email, password, nickname, gender, birth_year, height, weight,
                   goal, health_note, active, created_at
              FROM users
             WHERE user_id = ?
            """;

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapUser(resultSet) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("사용자 정보를 조회하지 못했습니다.", e);
        }
    }

    public synchronized User findByEmail(String email) {
        String sql = """
            SELECT user_id, email, password, nickname, gender, birth_year, height, weight,
                   goal, health_note, active, created_at
              FROM users
             WHERE LOWER(email) = LOWER(?)
            """;

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapUser(resultSet) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("이메일로 사용자를 조회하지 못했습니다.", e);
        }
    }

    public synchronized void save(User user) {
        Objects.requireNonNull(user, "user must not be null");

        String sql = """
            INSERT INTO users (
                user_id, email, password, nickname, gender, birth_year, height, weight,
                goal, health_note, active, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                email = VALUES(email),
                password = VALUES(password),
                nickname = VALUES(nickname),
                gender = VALUES(gender),
                birth_year = VALUES(birth_year),
                height = VALUES(height),
                weight = VALUES(weight),
                goal = VALUES(goal),
                health_note = VALUES(health_note),
                active = VALUES(active),
                created_at = VALUES(created_at),
                updated_at = VALUES(updated_at)
            """;

        LocalDateTime createdAt = user.getCreatedAt() == null ? LocalDateTime.now() : user.getCreatedAt();
        LocalDateTime updatedAt = LocalDateTime.now();

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getId());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getNickname());
            statement.setString(5, user.getGender());
            statement.setInt(6, user.getBirthYear());
            statement.setDouble(7, user.getHeight());
            statement.setDouble(8, user.getWeight());
            statement.setString(9, user.getGoal());
            statement.setString(10, user.getHealthNote());
            statement.setBoolean(11, user.isActive());
            statement.setTimestamp(12, Timestamp.valueOf(createdAt));
            statement.setTimestamp(13, Timestamp.valueOf(updatedAt));
            statement.executeUpdate();

            user.setCreatedAt(createdAt);
        } catch (SQLException e) {
            throw new IllegalStateException("사용자 정보를 저장하지 못했습니다.", e);
        }
    }

    public synchronized void delete(String userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("사용자 정보를 삭제하지 못했습니다.", e);
        }
    }

    private void initializeSchema() {
        try (Connection connection = DBUtil.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_USERS_SQL);
        } catch (SQLException e) {
            throw new IllegalStateException("UserRepository 테이블을 초기화하지 못했습니다.", e);
        }
    }

    private void seedIfEmpty(List<User> seedUsers) {
        if (seedUsers.isEmpty()) {
            return;
        }

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next() && resultSet.getInt(1) > 0) {
                return;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("users 테이블 상태를 확인하지 못했습니다.", e);
        }

        for (User seedUser : seedUsers) {
            save(seedUser);
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getString("user_id"));
        user.setEmail(resultSet.getString("email"));
        user.setPassword(resultSet.getString("password"));
        user.setNickname(resultSet.getString("nickname"));
        user.setGender(resultSet.getString("gender"));
        user.setBirthYear(resultSet.getInt("birth_year"));
        user.setHeight(resultSet.getDouble("height"));
        user.setWeight(resultSet.getDouble("weight"));
        user.setGoal(resultSet.getString("goal"));
        user.setHealthNote(resultSet.getString("health_note"));
        user.setActive(resultSet.getBoolean("active"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        user.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        return user;
    }
}
