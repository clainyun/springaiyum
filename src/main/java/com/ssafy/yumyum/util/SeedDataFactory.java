package com.ssafy.yumyum.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ssafy.yumyum.model.Challenge;
import com.ssafy.yumyum.model.ChallengeMembership;
import com.ssafy.yumyum.model.CommunityComment;
import com.ssafy.yumyum.model.CommunityPost;
import com.ssafy.yumyum.model.FollowRelation;
import com.ssafy.yumyum.model.FoodItem;
import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.model.User;

public final class SeedDataFactory {

    private static final String DEMO_PASSWORD_HASH = "$2a$10$6i0ksIIYtT0nWKwoYblkJO50B1sesL2GiPcYdREo3/8CyAsCUjka.";

    private SeedDataFactory() {
    }

    public static List<User> users() {
        LocalDateTime now = LocalDateTime.now().minusDays(7);
        List<User> users = new ArrayList<>();
        users.add(user("user_demo", "demo@yamyam.com", DEMO_PASSWORD_HASH, "데모 사용자", "male", 1996, 176, 72, "health", "", now));
        users.add(user("user_mina", "mina@yamyam.com", DEMO_PASSWORD_HASH, "민아", "female", 1998, 164, 55, "diet", "당 섭취 과다 주의", now.plusHours(1)));
        users.add(user("user_joon", "joon@yamyam.com", DEMO_PASSWORD_HASH, "준호", "male", 1994, 181, 81, "muscle", "", now.plusHours(2)));
        users.add(user("user_hana", "hana@yamyam.com", DEMO_PASSWORD_HASH, "하나", "female", 1997, 168, 60, "health", "짠 음식 주의", now.plusHours(3)));
        return users;
    }

    private static User user(String id, String email, String password, String nickname, String gender,
                             int birthYear, double height, double weight, String goal, String note, LocalDateTime createdAt) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPassword(password);
        user.setNickname(nickname);
        user.setGender(gender);
        user.setBirthYear(birthYear);
        user.setHeight(height);
        user.setWeight(weight);
        user.setGoal(goal);
        user.setHealthNote(note);
        user.setActive(true);
        user.setCreatedAt(createdAt);
        return user;
    }

    public static List<FoodItem> catalogFoods() {
        return Arrays.asList(
            food("food_oat", "오트밀", "아침식사", 100, 380, 66, 13, 7),
            food("food_egg", "삶은 달걀", "단백질", 100, 156, 1.1, 13, 11),
            food("food_yogurt", "그릭요거트", "간식", 100, 95, 4, 10, 5),
            food("food_banana", "바나나", "과일", 100, 89, 23, 1.1, 0.3),
            food("food_rice", "현미밥", "탄수화물", 100, 150, 32, 3.2, 1.1),
            food("food_chicken", "닭가슴살", "단백질", 100, 165, 0, 31, 3.6),
            food("food_salad", "샐러드 믹스", "채소", 100, 32, 6, 2, 0.4),
            food("food_salmon", "연어구이", "단백질", 100, 208, 0, 20, 13),
            food("food_potato", "구운 감자", "탄수화물", 100, 93, 21, 2.5, 0.1),
            food("food_tofu", "두부", "단백질", 100, 84, 2, 9, 5),
            food("food_apple", "사과", "과일", 100, 52, 14, 0.3, 0.2),
            food("food_broccoli", "브로콜리", "채소", 100, 34, 7, 2.8, 0.4),
            food("food_soup", "된장국", "국물", 100, 38, 4.1, 2.7, 1.2),
            food("food_nuts", "아몬드", "간식", 100, 579, 22, 21, 50),
            food("food_sweetpotato", "고구마", "탄수화물", 100, 128, 30, 1.6, 0.2)
        );
    }

    private static FoodItem food(String code, String name, String category, double grams,
                                 double energy, double carbs, double protein, double fat) {
        FoodItem item = new FoodItem();
        item.setCode(code);
        item.setName(name);
        item.setCategory(category);
        item.setGrams(grams);
        item.setEnergy(energy);
        item.setCarbs(carbs);
        item.setProtein(protein);
        item.setFat(fat);
        return item;
    }

    public static List<Meal> meals() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate twoDaysAgo = today.minusDays(2);
        LocalDateTime now = LocalDateTime.now().minusDays(2);

        List<Meal> meals = new ArrayList<>();
        meals.add(meal("meal_demo_breakfast", "user_demo", today, "breakfast", "오전에 포만감을 유지하는 아침 식단입니다.", now,
            food("food_oat", "오트밀", "아침식사", 100, 380, 66, 13, 7).copyWithGrams(180),
            food("food_egg", "삶은 달걀", "단백질", 100, 156, 1.1, 13, 11).copyWithGrams(100),
            food("food_banana", "바나나", "과일", 100, 89, 23, 1.1, 0.3).copyWithGrams(100)
        ));
        meals.add(meal("meal_demo_lunch", "user_demo", today, "lunch", "채소와 단백질을 같이 챙긴 점심입니다.", now.plusHours(3),
            food("food_rice", "현미밥", "탄수화물", 100, 150, 32, 3.2, 1.1).copyWithGrams(180),
            food("food_chicken", "닭가슴살", "단백질", 100, 165, 0, 31, 3.6).copyWithGrams(160),
            food("food_salad", "샐러드 믹스", "채소", 100, 32, 6, 2, 0.4).copyWithGrams(120),
            food("food_soup", "된장국", "국물", 100, 38, 4.1, 2.7, 1.2).copyWithGrams(150)
        ));
        meals.add(meal("meal_demo_dinner", "user_demo", yesterday, "dinner", "회복을 고려한 가벼운 저녁입니다.", now.minusDays(1),
            food("food_salmon", "연어구이", "단백질", 100, 208, 0, 20, 13).copyWithGrams(150),
            food("food_potato", "구운 감자", "탄수화물", 100, 93, 21, 2.5, 0.1).copyWithGrams(170),
            food("food_broccoli", "브로콜리", "채소", 100, 34, 7, 2.8, 0.4).copyWithGrams(100)
        ));
        meals.add(meal("meal_demo_snack", "user_demo", twoDaysAgo, "snack", "운동 전 간식입니다.", now.minusDays(2),
            food("food_yogurt", "그릭요거트", "간식", 100, 95, 4, 10, 5).copyWithGrams(150),
            food("food_apple", "사과", "과일", 100, 52, 14, 0.3, 0.2).copyWithGrams(120)
        ));
        return meals;
    }

    private static Meal meal(String id, String userId, LocalDate mealDate, String mealType, String memo,
                             LocalDateTime createdAt, FoodItem... foods) {
        Meal meal = new Meal();
        meal.setId(id);
        meal.setUserId(userId);
        meal.setMealDate(mealDate);
        meal.setMealType(mealType);
        meal.setMemo(memo);
        meal.setFoods(new ArrayList<>(Arrays.asList(foods)));
        meal.setCreatedAt(createdAt);
        meal.setUpdatedAt(createdAt);
        return meal;
    }

    public static List<FollowRelation> follows() {
        LocalDateTime now = LocalDateTime.now().minusDays(4);
        List<FollowRelation> result = new ArrayList<>();
        result.add(follow("follow_1", "user_demo", "user_mina", now));
        result.add(follow("follow_2", "user_demo", "user_joon", now.plusHours(1)));
        result.add(follow("follow_3", "user_hana", "user_demo", now.plusHours(2)));
        return result;
    }

    private static FollowRelation follow(String id, String followerId, String followeeId, LocalDateTime createdAt) {
        FollowRelation relation = new FollowRelation();
        relation.setId(id);
        relation.setFollowerId(followerId);
        relation.setFolloweeId(followeeId);
        relation.setCreatedAt(createdAt);
        return relation;
    }

    public static List<Challenge> challenges() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now().minusDays(3);
        List<Challenge> challenges = new ArrayList<>();
        challenges.add(challenge("challenge_breakfast", "7일 아침 기록 챌린지", "매일 아침 식단을 기록해 보세요.", "식습관", 7, today, today.plusDays(6), "user_demo", now));
        challenges.add(challenge("challenge_protein", "단백질 채우기 챌린지", "5일 동안 단백질 목표를 달성해 보세요.", "영양 관리", 5, today, today.plusDays(10), "user_joon", now.plusHours(2)));
        challenges.add(challenge("challenge_walk", "식후 걷기 챌린지", "식사 후 15분 걷기를 4회 실천합니다.", "운동", 4, today, today.plusDays(8), "user_hana", now.plusHours(3)));
        return challenges;
    }

    private static Challenge challenge(String id, String title, String description, String category, int targetCount,
                                       LocalDate startDate, LocalDate endDate, String createdBy, LocalDateTime createdAt) {
        Challenge challenge = new Challenge();
        challenge.setId(id);
        challenge.setTitle(title);
        challenge.setDescription(description);
        challenge.setCategory(category);
        challenge.setTargetCount(targetCount);
        challenge.setStartDate(startDate);
        challenge.setEndDate(endDate);
        challenge.setCreatedBy(createdBy);
        challenge.setCreatedAt(createdAt);
        return challenge;
    }

    public static List<ChallengeMembership> memberships() {
        LocalDateTime now = LocalDateTime.now().minusDays(2);
        List<ChallengeMembership> memberships = new ArrayList<>();
        memberships.add(membership("membership_1", "challenge_breakfast", "user_demo", 3, "joined", now));
        memberships.add(membership("membership_2", "challenge_walk", "user_demo", 1, "joined", now.plusHours(1)));
        memberships.add(membership("membership_3", "challenge_protein", "user_joon", 4, "joined", now.plusHours(2)));
        return memberships;
    }

    private static ChallengeMembership membership(String id, String challengeId, String userId, int progress, String status, LocalDateTime joinedAt) {
        ChallengeMembership membership = new ChallengeMembership();
        membership.setId(id);
        membership.setChallengeId(challengeId);
        membership.setUserId(userId);
        membership.setProgress(progress);
        membership.setStatus(status);
        membership.setJoinedAt(joinedAt);
        membership.setUpdatedAt(joinedAt);
        return membership;
    }

    public static List<CommunityPost> posts() {
        LocalDateTime now = LocalDateTime.now().minusHours(12);
        List<CommunityPost> posts = new ArrayList<>();
        posts.add(post("post_1", "user_mina", "review", "meal_demo_dinner", "저녁 식단을 가볍게 먹는 팁", "단백질은 유지하고 탄수화물 양을 조금 줄이니 다음날 컨디션이 좋았습니다.", now));
        posts.add(post("post_2", "user_joon", "expert", null, "단백질 섭취를 나누는 방법", "아침, 점심, 저녁에 고르게 배분하면 포만감과 회복에 도움이 됩니다.", now.minusHours(2)));
        posts.add(post("post_3", "user_hana", "free", null, "빠르게 준비할 수 있는 아침 메뉴 추천", "10분 안에 만들 수 있으면서 든든한 메뉴가 궁금합니다.", now.minusHours(4)));
        return posts;
    }

    private static CommunityPost post(String id, String userId, String category, String linkedMealId,
                                      String title, String content, LocalDateTime createdAt) {
        CommunityPost post = new CommunityPost();
        post.setId(id);
        post.setUserId(userId);
        post.setCategory(category);
        post.setLinkedMealId(linkedMealId);
        post.setTitle(title);
        post.setContent(content);
        post.setCreatedAt(createdAt);
        post.setUpdatedAt(createdAt);
        return post;
    }

    public static List<CommunityComment> comments() {
        LocalDateTime now = LocalDateTime.now().minusHours(5);
        List<CommunityComment> comments = new ArrayList<>();
        comments.add(comment("comment_1", "post_1", "user_demo", "샐러드와 국물을 같이 두니 포만감이 유지돼서 좋았습니다.", now));
        comments.add(comment("comment_2", "post_3", "user_mina", "그릭요거트와 바나나 조합도 준비가 빨라서 자주 먹어요.", now.minusHours(1)));
        return comments;
    }

    private static CommunityComment comment(String id, String postId, String userId, String content, LocalDateTime createdAt) {
        CommunityComment comment = new CommunityComment();
        comment.setId(id);
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreatedAt(createdAt);
        comment.setUpdatedAt(createdAt);
        return comment;
    }
}
