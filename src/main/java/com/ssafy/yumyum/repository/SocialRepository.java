package com.ssafy.yumyum.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ssafy.yumyum.model.FollowRelation;
import com.ssafy.yumyum.util.SeedDataFactory;

import org.springframework.stereotype.Repository;

@Repository
public class SocialRepository {

    private final Map<String, FollowRelation> follows = new LinkedHashMap<>();

    public SocialRepository() {
        this(SeedDataFactory.follows());
    }

    public SocialRepository(List<FollowRelation> seedRelations) {
        for (FollowRelation relation : seedRelations) {
            follows.put(relation.getId(), relation);
        }
    }

    public synchronized List<FollowRelation> findAll() {
        return new ArrayList<>(follows.values());
    }

    public synchronized void save(FollowRelation relation) {
        follows.put(relation.getId(), relation);
    }

    public synchronized void delete(String relationId) {
        follows.remove(relationId);
    }
}
