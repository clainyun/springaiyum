package com.ssafy.yumyum.util;

import com.ssafy.yumyum.service.AuthService;
import com.ssafy.yumyum.service.ChallengeService;
import com.ssafy.yumyum.service.CoachService;
import com.ssafy.yumyum.service.CommunityService;
import com.ssafy.yumyum.service.MealService;
import com.ssafy.yumyum.service.SocialService;
import com.ssafy.yumyum.service.UserService;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public final class AppContainer implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    public static AuthService getAuthService() {
        return getBean(AuthService.class);
    }

    public static UserService getUserService() {
        return getBean(UserService.class);
    }

    public static MealService getMealService() {
        return getBean(MealService.class);
    }

    public static SocialService getSocialService() {
        return getBean(SocialService.class);
    }

    public static ChallengeService getChallengeService() {
        return getBean(ChallengeService.class);
    }

    public static CommunityService getCommunityService() {
        return getBean(CommunityService.class);
    }

    public static CoachService getCoachService() {
        return getBean(CoachService.class);
    }

    private static <T> T getBean(Class<T> beanType) {
        if (applicationContext == null) {
            throw new IllegalStateException("Spring application context has not been initialized.");
        }
        return applicationContext.getBean(beanType);
    }
}
