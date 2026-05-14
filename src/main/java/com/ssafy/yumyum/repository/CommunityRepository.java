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

import com.ssafy.yumyum.model.CommunityComment;
import com.ssafy.yumyum.model.CommunityPost;
import com.ssafy.yumyum.util.DBUtil;
import com.ssafy.yumyum.util.SeedDataFactory;

import org.springframework.stereotype.Repository;

@Repository
public class CommunityRepository {

    private static final String CREATE_POSTS_SQL = """
        CREATE TABLE IF NOT EXISTS community_posts (
            post_id VARCHAR(64) NOT NULL PRIMARY KEY,
            user_id VARCHAR(64) NOT NULL,
            category VARCHAR(30) NULL,
            linked_meal_id VARCHAR(64) NULL,
            title VARCHAR(200) NOT NULL,
            content TEXT NOT NULL,
            created_at DATETIME NOT NULL,
            updated_at DATETIME NOT NULL,
            INDEX idx_community_posts_user_created (user_id, created_at),
            INDEX idx_community_posts_category_created (category, created_at)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """;

    private static final String CREATE_COMMENTS_SQL = """
        CREATE TABLE IF NOT EXISTS community_comments (
            comment_id VARCHAR(64) NOT NULL PRIMARY KEY,
            post_id VARCHAR(64) NOT NULL,
            user_id VARCHAR(64) NOT NULL,
            content TEXT NOT NULL,
            created_at DATETIME NOT NULL,
            updated_at DATETIME NOT NULL,
            INDEX idx_community_comments_post_created (post_id, created_at),
            CONSTRAINT fk_community_comments_post_id
                FOREIGN KEY (post_id) REFERENCES community_posts (post_id) ON DELETE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """;

    private static final String POST_SELECT_COLUMNS = """
        SELECT post_id, user_id, category, linked_meal_id, title, content, created_at, updated_at
          FROM community_posts
        """;
    private static final String FIND_ALL_POSTS_SQL = POST_SELECT_COLUMNS + " ORDER BY created_at ASC, post_id ASC";
    private static final String FIND_POST_BY_ID_SQL = POST_SELECT_COLUMNS + " WHERE post_id = ?";

    private static final String COMMENT_SELECT_COLUMNS = """
        SELECT comment_id, post_id, user_id, content, created_at, updated_at
          FROM community_comments
        """;
    private static final String FIND_ALL_COMMENTS_SQL = COMMENT_SELECT_COLUMNS + " ORDER BY created_at ASC, comment_id ASC";
    private static final String FIND_COMMENT_BY_ID_SQL = COMMENT_SELECT_COLUMNS + " WHERE comment_id = ?";

    public CommunityRepository() {
        this(SeedDataFactory.posts(), SeedDataFactory.comments());
    }

    public CommunityRepository(List<CommunityPost> seedPosts, List<CommunityComment> seedComments) {
        initializeSchema();
        seedIfEmpty(seedPosts == null ? List.of() : seedPosts, seedComments == null ? List.of() : seedComments);
    }

    public synchronized List<CommunityPost> findAllPosts() {
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_POSTS_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            return mapPosts(resultSet);
        } catch (SQLException e) {
            throw repositoryException("find all community posts", e);
        }
    }

    public synchronized CommunityPost findPostById(String postId) {
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_POST_BY_ID_SQL)) {
            statement.setString(1, postId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapPost(resultSet) : null;
            }
        } catch (SQLException e) {
            throw repositoryException("find community post by id", e);
        }
    }

    public synchronized void savePost(CommunityPost post) {
        Objects.requireNonNull(post, "post must not be null");

        String sql = """
            INSERT INTO community_posts (
                post_id, user_id, category, linked_meal_id, title, content, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                user_id = VALUES(user_id),
                category = VALUES(category),
                linked_meal_id = VALUES(linked_meal_id),
                title = VALUES(title),
                content = VALUES(content),
                created_at = VALUES(created_at),
                updated_at = VALUES(updated_at)
            """;

        LocalDateTime createdAt = post.getCreatedAt() == null ? LocalDateTime.now() : post.getCreatedAt();
        LocalDateTime updatedAt = post.getUpdatedAt() == null ? createdAt : post.getUpdatedAt();

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindPost(statement, post, createdAt, updatedAt);
            statement.executeUpdate();

            post.setCreatedAt(createdAt);
            post.setUpdatedAt(updatedAt);
        } catch (SQLException e) {
            throw repositoryException("save community post", e);
        }
    }

    public synchronized void deletePost(String postId) {
        String sql = "DELETE FROM community_posts WHERE post_id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, postId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw repositoryException("delete community post", e);
        }
    }

    public synchronized List<CommunityComment> findAllComments() {
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_COMMENTS_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            return mapComments(resultSet);
        } catch (SQLException e) {
            throw repositoryException("find all community comments", e);
        }
    }

    public synchronized CommunityComment findCommentById(String commentId) {
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_COMMENT_BY_ID_SQL)) {
            statement.setString(1, commentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapComment(resultSet) : null;
            }
        } catch (SQLException e) {
            throw repositoryException("find community comment by id", e);
        }
    }

    public synchronized void saveComment(CommunityComment comment) {
        Objects.requireNonNull(comment, "comment must not be null");

        String sql = """
            INSERT INTO community_comments (
                comment_id, post_id, user_id, content, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                post_id = VALUES(post_id),
                user_id = VALUES(user_id),
                content = VALUES(content),
                created_at = VALUES(created_at),
                updated_at = VALUES(updated_at)
            """;

        LocalDateTime createdAt = comment.getCreatedAt() == null ? LocalDateTime.now() : comment.getCreatedAt();
        LocalDateTime updatedAt = comment.getUpdatedAt() == null ? createdAt : comment.getUpdatedAt();

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindComment(statement, comment, createdAt, updatedAt);
            statement.executeUpdate();

            comment.setCreatedAt(createdAt);
            comment.setUpdatedAt(updatedAt);
        } catch (SQLException e) {
            throw repositoryException("save community comment", e);
        }
    }

    public synchronized void deleteComment(String commentId) {
        String sql = "DELETE FROM community_comments WHERE comment_id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, commentId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw repositoryException("delete community comment", e);
        }
    }

    private void initializeSchema() {
        try (Connection connection = DBUtil.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_POSTS_SQL);
            statement.executeUpdate(CREATE_COMMENTS_SQL);
        } catch (SQLException e) {
            throw repositoryException("initialize community tables", e);
        }
    }

    private void seedIfEmpty(List<CommunityPost> seedPosts, List<CommunityComment> seedComments) {
        if (seedPosts.isEmpty() && seedComments.isEmpty()) {
            return;
        }

        try (Connection connection = DBUtil.getConnection()) {
            if (hasExistingPosts(connection)) {
                return;
            }

            connection.setAutoCommit(false);
            try {
                for (CommunityPost seedPost : seedPosts) {
                    persistPost(connection, seedPost);
                }
                for (CommunityComment seedComment : seedComments) {
                    persistComment(connection, seedComment);
                }
                connection.commit();
            } catch (SQLException e) {
                rollbackQuietly(connection);
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw repositoryException("seed community data", e);
        }
    }

    private boolean hasExistingPosts(Connection connection) throws SQLException {
        try (PreparedStatement countStatement = connection.prepareStatement("SELECT COUNT(*) FROM community_posts");
             ResultSet resultSet = countStatement.executeQuery()) {
            return resultSet.next() && resultSet.getInt(1) > 0;
        }
    }

    private void persistPost(Connection connection, CommunityPost post) throws SQLException {
        String sql = """
            INSERT INTO community_posts (
                post_id, user_id, category, linked_meal_id, title, content, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                user_id = VALUES(user_id),
                category = VALUES(category),
                linked_meal_id = VALUES(linked_meal_id),
                title = VALUES(title),
                content = VALUES(content),
                created_at = VALUES(created_at),
                updated_at = VALUES(updated_at)
            """;

        LocalDateTime createdAt = post.getCreatedAt() == null ? LocalDateTime.now() : post.getCreatedAt();
        LocalDateTime updatedAt = post.getUpdatedAt() == null ? createdAt : post.getUpdatedAt();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindPost(statement, post, createdAt, updatedAt);
            statement.executeUpdate();
        }
    }

    private void persistComment(Connection connection, CommunityComment comment) throws SQLException {
        String sql = """
            INSERT INTO community_comments (
                comment_id, post_id, user_id, content, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                post_id = VALUES(post_id),
                user_id = VALUES(user_id),
                content = VALUES(content),
                created_at = VALUES(created_at),
                updated_at = VALUES(updated_at)
            """;

        LocalDateTime createdAt = comment.getCreatedAt() == null ? LocalDateTime.now() : comment.getCreatedAt();
        LocalDateTime updatedAt = comment.getUpdatedAt() == null ? createdAt : comment.getUpdatedAt();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindComment(statement, comment, createdAt, updatedAt);
            statement.executeUpdate();
        }
    }

    private void bindPost(PreparedStatement statement, CommunityPost post, LocalDateTime createdAt,
                          LocalDateTime updatedAt) throws SQLException {
        statement.setString(1, post.getId());
        statement.setString(2, post.getUserId());
        statement.setString(3, post.getCategory());
        statement.setString(4, post.getLinkedMealId());
        statement.setString(5, post.getTitle());
        statement.setString(6, post.getContent());
        statement.setTimestamp(7, Timestamp.valueOf(createdAt));
        statement.setTimestamp(8, Timestamp.valueOf(updatedAt));
    }

    private void bindComment(PreparedStatement statement, CommunityComment comment, LocalDateTime createdAt,
                             LocalDateTime updatedAt) throws SQLException {
        statement.setString(1, comment.getId());
        statement.setString(2, comment.getPostId());
        statement.setString(3, comment.getUserId());
        statement.setString(4, comment.getContent());
        statement.setTimestamp(5, Timestamp.valueOf(createdAt));
        statement.setTimestamp(6, Timestamp.valueOf(updatedAt));
    }

    private List<CommunityPost> mapPosts(ResultSet resultSet) throws SQLException {
        List<CommunityPost> posts = new ArrayList<>();
        while (resultSet.next()) {
            posts.add(mapPost(resultSet));
        }
        return posts;
    }

    private List<CommunityComment> mapComments(ResultSet resultSet) throws SQLException {
        List<CommunityComment> comments = new ArrayList<>();
        while (resultSet.next()) {
            comments.add(mapComment(resultSet));
        }
        return comments;
    }

    private CommunityPost mapPost(ResultSet resultSet) throws SQLException {
        CommunityPost post = new CommunityPost();
        post.setId(resultSet.getString("post_id"));
        post.setUserId(resultSet.getString("user_id"));
        post.setCategory(resultSet.getString("category"));
        post.setLinkedMealId(resultSet.getString("linked_meal_id"));
        post.setTitle(resultSet.getString("title"));
        post.setContent(resultSet.getString("content"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        post.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        post.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());
        return post;
    }

    private CommunityComment mapComment(ResultSet resultSet) throws SQLException {
        CommunityComment comment = new CommunityComment();
        comment.setId(resultSet.getString("comment_id"));
        comment.setPostId(resultSet.getString("post_id"));
        comment.setUserId(resultSet.getString("user_id"));
        comment.setContent(resultSet.getString("content"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        comment.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        comment.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());
        return comment;
    }

    private void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    private IllegalStateException repositoryException(String action, SQLException cause) {
        return new IllegalStateException("Failed to " + action + ".", cause);
    }
}
