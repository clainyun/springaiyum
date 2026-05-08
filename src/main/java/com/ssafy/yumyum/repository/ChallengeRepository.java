package com.ssafy.yumyum.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ssafy.yumyum.model.Challenge;
import com.ssafy.yumyum.model.ChallengeMembership;

public class ChallengeRepository {

    private final Map<String, Challenge> challenges = new LinkedHashMap<>();
    private final Map<String, ChallengeMembership> memberships = new LinkedHashMap<>();

    public ChallengeRepository(List<Challenge> seedChallenges, List<ChallengeMembership> seedMemberships) {
        for (Challenge challenge : seedChallenges) {
            challenges.put(challenge.getId(), challenge);
        }
        for (ChallengeMembership membership : seedMemberships) {
            memberships.put(membership.getId(), membership);
        }
    }

    public synchronized List<Challenge> findAllChallenges() {
        return new ArrayList<>(challenges.values());
    }

    public synchronized Challenge findChallengeById(String challengeId) {
        return challenges.get(challengeId);
    }

    public synchronized void saveChallenge(Challenge challenge) {
        challenges.put(challenge.getId(), challenge);
    }

    public synchronized void deleteChallenge(String challengeId) {
        challenges.remove(challengeId);
        List<String> targets = new ArrayList<>();
        for (ChallengeMembership membership : memberships.values()) {
            if (challengeId.equals(membership.getChallengeId())) {
                targets.add(membership.getId());
            }
        }
        for (String target : targets) {
            memberships.remove(target);
        }
    }

    public synchronized List<ChallengeMembership> findAllMemberships() {
        return new ArrayList<>(memberships.values());
    }

    public synchronized void saveMembership(ChallengeMembership membership) {
        memberships.put(membership.getId(), membership);
    }

    public synchronized void deleteMembership(String membershipId) {
        memberships.remove(membershipId);
    }
}
