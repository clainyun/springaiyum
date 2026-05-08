package com.ssafy.yumyum.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ssafy.yumyum.model.User;

public class UserRepository {

    private final Map<String, User> users = new LinkedHashMap<>();

    public UserRepository(List<User> seedUsers) {
        for (User user : seedUsers) {
            users.put(user.getId(), user);
        }
    }

    public synchronized List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public synchronized User findById(String id) {
        return users.get(id);
    }

    public synchronized User findByEmail(String email) {
        for (User user : users.values()) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    public synchronized void save(User user) {
        users.put(user.getId(), user);
    }

    public synchronized void delete(String userId) {
        users.remove(userId);
    }
}
