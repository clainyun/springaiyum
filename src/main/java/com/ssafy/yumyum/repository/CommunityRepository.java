package com.ssafy.yumyum.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ssafy.yumyum.model.CommunityComment;
import com.ssafy.yumyum.model.CommunityPost;

public class CommunityRepository {

    private final Map<String, CommunityPost> posts = new LinkedHashMap<>();
    private final Map<String, CommunityComment> comments = new LinkedHashMap<>();

    public CommunityRepository(List<CommunityPost> seedPosts, List<CommunityComment> seedComments) {
        for (CommunityPost post : seedPosts) {
            posts.put(post.getId(), post);
        }
        for (CommunityComment comment : seedComments) {
            comments.put(comment.getId(), comment);
        }
    }

    public synchronized List<CommunityPost> findAllPosts() {
        return new ArrayList<>(posts.values());
    }

    public synchronized CommunityPost findPostById(String postId) {
        return posts.get(postId);
    }

    public synchronized void savePost(CommunityPost post) {
        posts.put(post.getId(), post);
    }

    public synchronized void deletePost(String postId) {
        posts.remove(postId);
        List<String> targets = new ArrayList<>();
        for (CommunityComment comment : comments.values()) {
            if (postId.equals(comment.getPostId())) {
                targets.add(comment.getId());
            }
        }
        for (String target : targets) {
            comments.remove(target);
        }
    }

    public synchronized List<CommunityComment> findAllComments() {
        return new ArrayList<>(comments.values());
    }

    public synchronized CommunityComment findCommentById(String commentId) {
        return comments.get(commentId);
    }

    public synchronized void saveComment(CommunityComment comment) {
        comments.put(comment.getId(), comment);
    }

    public synchronized void deleteComment(String commentId) {
        comments.remove(commentId);
    }
}
