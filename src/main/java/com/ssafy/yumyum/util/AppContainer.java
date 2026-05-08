package com.ssafy.yumyum.util;

import com.ssafy.yumyum.repository.ChallengeRepository;
import com.ssafy.yumyum.repository.CommunityRepository;
import com.ssafy.yumyum.repository.FoodCatalogRepository;
import com.ssafy.yumyum.repository.MealRepository;
import com.ssafy.yumyum.repository.SocialRepository;
import com.ssafy.yumyum.repository.UserRepository;
import com.ssafy.yumyum.service.AuthService;
import com.ssafy.yumyum.service.ChallengeService;
import com.ssafy.yumyum.service.CoachService;
import com.ssafy.yumyum.service.CommunityService;
import com.ssafy.yumyum.service.MealService;
import com.ssafy.yumyum.service.SocialService;
import com.ssafy.yumyum.service.UserService;

public final class AppContainer {

    private static final UserRepository USER_REPOSITORY = new UserRepository(SeedDataFactory.users());
    private static final MealRepository MEAL_REPOSITORY = new MealRepository(SeedDataFactory.meals());
    private static final FoodCatalogRepository FOOD_CATALOG_REPOSITORY = new FoodCatalogRepository(SeedDataFactory.catalogFoods());
    private static final SocialRepository SOCIAL_REPOSITORY = new SocialRepository(SeedDataFactory.follows());
    private static final ChallengeRepository CHALLENGE_REPOSITORY = new ChallengeRepository(SeedDataFactory.challenges(), SeedDataFactory.memberships());
    private static final CommunityRepository COMMUNITY_REPOSITORY = new CommunityRepository(SeedDataFactory.posts(), SeedDataFactory.comments());

    private static final MealService MEAL_SERVICE = new MealService(MEAL_REPOSITORY, FOOD_CATALOG_REPOSITORY);
    private static final SocialService SOCIAL_SERVICE = new SocialService(SOCIAL_REPOSITORY, USER_REPOSITORY);
    private static final ChallengeService CHALLENGE_SERVICE = new ChallengeService(CHALLENGE_REPOSITORY, USER_REPOSITORY);
    private static final CommunityService COMMUNITY_SERVICE = new CommunityService(COMMUNITY_REPOSITORY, USER_REPOSITORY, MEAL_REPOSITORY);
    private static final UserService USER_SERVICE = new UserService(USER_REPOSITORY, MEAL_REPOSITORY, SOCIAL_REPOSITORY, CHALLENGE_REPOSITORY, COMMUNITY_REPOSITORY);
    private static final AuthService AUTH_SERVICE = new AuthService(USER_REPOSITORY);
    private static final CoachService COACH_SERVICE = new CoachService(MEAL_SERVICE, CHALLENGE_SERVICE);

    private AppContainer() {
    }

    public static AuthService getAuthService() {
        return AUTH_SERVICE;
    }

    public static UserService getUserService() {
        return USER_SERVICE;
    }

    public static MealService getMealService() {
        return MEAL_SERVICE;
    }

    public static SocialService getSocialService() {
        return SOCIAL_SERVICE;
    }

    public static ChallengeService getChallengeService() {
        return CHALLENGE_SERVICE;
    }

    public static CommunityService getCommunityService() {
        return COMMUNITY_SERVICE;
    }

    public static CoachService getCoachService() {
        return COACH_SERVICE;
    }
}
