package com.ssafy.yumyum.repository;

import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY user_id DESC";

        List<User> users = new ArrayList<>();

        try (
            Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()
        ) {

            while (rs.next()) {
                users.add(mapToUser(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("회원 목록 조회 실패", e);
        }

        return users;
    }

    public User findById(String id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (
            Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setInt(1, Integer.parseInt(id));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToUser(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("회원 조회 실패", e);
        }

        return null;
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (
            Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToUser(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("이메일 조회 실패", e);
        }

        return null;
    }

    public void save(User user) {

        if (user.getId() == null || user.getId().isEmpty()) {
            insert(user);
        } else {
            update(user);
        }
    }

    private void insert(User user) {

        String sql = """
            INSERT INTO users (
                email,
                password,
                nickname,
                gender,
                birth_year,
                height,
                weight,
                goal,
                health_note,
                active
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (
            Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {

            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getNickname());
            pstmt.setString(4, user.getGender());
            pstmt.setInt(5, user.getBirthYear());
            pstmt.setDouble(6, user.getHeight());
            pstmt.setDouble(7, user.getWeight());
            pstmt.setString(8, user.getGoal());
            pstmt.setString(9, user.getHealthNote());
            pstmt.setBoolean(10, user.isActive());

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(String.valueOf(rs.getInt(1)));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("회원 등록 실패", e);
        }
    }

    private void update(User user) {

        String sql = """
            UPDATE users
            SET
                email = ?,
                password = ?,
                nickname = ?,
                gender = ?,
                birth_year = ?,
                height = ?,
                weight = ?,
                goal = ?,
                health_note = ?,
                active = ?
            WHERE user_id = ?
        """;

        try (
            Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getNickname());
            pstmt.setString(4, user.getGender());
            pstmt.setInt(5, user.getBirthYear());
            pstmt.setDouble(6, user.getHeight());
            pstmt.setDouble(7, user.getWeight());
            pstmt.setString(8, user.getGoal());
            pstmt.setString(9, user.getHealthNote());
            pstmt.setBoolean(10, user.isActive());
            pstmt.setInt(11, Integer.parseInt(user.getId()));

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("회원 수정 실패", e);
        }
    }

    public void delete(String userId) {

        String sql = "DELETE FROM users WHERE user_id = ?";

        try (
            Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setInt(1, Integer.parseInt(userId));

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("회원 삭제 실패", e);
        }
    }

    private User mapToUser(ResultSet rs) throws SQLException {

        User user = new User();

        user.setId(String.valueOf(rs.getInt("user_id")));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setNickname(rs.getString("nickname"));
        user.setGender(rs.getString("gender"));
        user.setBirthYear(rs.getInt("birth_year"));
        user.setHeight(rs.getDouble("height"));
        user.setWeight(rs.getDouble("weight"));
        user.setGoal(rs.getString("goal"));
        user.setHealthNote(rs.getString("health_note"));
        user.setActive(rs.getBoolean("active"));

        Timestamp createdAt = rs.getTimestamp("created_at");

        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        } else {
            user.setCreatedAt(LocalDateTime.now());
        }

        return user;
    }
}