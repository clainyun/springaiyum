package com.ssafy.yumyum.controller.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import com.ssafy.yumyum.exception.ApiExceptionHandler;
import com.ssafy.yumyum.model.FoodItem;
import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.model.NutritionSummary;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.repository.UserRepository;
import com.ssafy.yumyum.service.MealService;
import com.ssafy.yumyum.util.ServiceResult;

@WebMvcTest({FoodApiController.class, MealApiController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class MealFoodApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MealService mealService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void searchFoodsReturns200() throws Exception {
        when(mealService.searchFoods("오트"))
                .thenReturn(List.of(food("food_oat", "오트밀", 100.0)));

        mockMvc.perform(get("/api/v1/foods")
                        .param("keyword", "오트")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("food_oat"))
                .andExpect(jsonPath("$[0].name").value("오트밀"));
    }

    @Test
    void getMealsWithoutSessionReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/meals")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("로그인이 필요합니다."));
    }

    @Test
    void getMealsWithSessionReturns200() throws Exception {
        User user = demoUser();
        Meal meal = meal("meal_demo_lunch", user.getId());

        when(userRepository.findById("user_demo")).thenReturn(user);
        when(mealService.getMealsForUser(eq(user.getId()), any(), any(), any(), eq("dateDesc"), eq(user)))
                .thenReturn(List.of(meal));
        when(mealService.summarize(meal.getFoods())).thenReturn(summary());

        mockMvc.perform(get("/api/v1/meals")
                        .session(loginSession())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("meal_demo_lunch"))
                .andExpect(jsonPath("$[0].nutrition.calories").value(380.0));
    }

    @Test
    void createMealReturns201() throws Exception {
        User user = demoUser();
        Meal meal = meal("meal_new", user.getId());

        when(userRepository.findById("user_demo")).thenReturn(user);
        when(mealService.findFood("food_oat")).thenReturn(food("food_oat", "오트밀", 100.0));
        when(mealService.createMeal(eq(user), any(), eq("lunch"), eq("점심 메모"), anyList()))
                .thenReturn(ServiceResult.success("식단을 등록했습니다.", meal));
        when(mealService.summarize(meal.getFoods())).thenReturn(summary());

        mockMvc.perform(post("/api/v1/meals")
                        .session(loginSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mealDate": "2026-05-22",
                                  "mealType": "lunch",
                                  "memo": "점심 메모",
                                  "foods": [
                                    { "code": "food_oat", "grams": 180.0 }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("meal_new"))
                .andExpect(jsonPath("$.mealType").value("lunch"));
    }

    @Test
    void updateMealReturns200() throws Exception {
        User user = demoUser();
        Meal meal = meal("meal_demo_lunch", user.getId());

        when(userRepository.findById("user_demo")).thenReturn(user);
        when(mealService.findFood("food_oat")).thenReturn(food("food_oat", "오트밀", 100.0));
        when(mealService.updateMeal(eq(user), eq("meal_demo_lunch"), any(), eq("lunch"), eq("수정 메모"), anyList()))
                .thenReturn(ServiceResult.success("식단을 수정했습니다.", meal));
        when(mealService.summarize(meal.getFoods())).thenReturn(summary());

        mockMvc.perform(put("/api/v1/meals/meal_demo_lunch")
                        .session(loginSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mealDate": "2026-05-22",
                                  "mealType": "lunch",
                                  "memo": "수정 메모",
                                  "foods": [
                                    { "code": "food_oat", "grams": 150.0 }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("meal_demo_lunch"));
    }

    private MockHttpSession loginSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUserId", "user_demo");
        return session;
    }

    private User demoUser() {
        User user = new User();
        user.setId("user_demo");
        user.setEmail("demo@yamyam.com");
        user.setNickname("데모 사용자");
        user.setGender("male");
        user.setBirthYear(1996);
        user.setHeight(176);
        user.setWeight(72);
        user.setGoal("health");
        user.setHealthNote("");
        user.setActive(true);
        return user;
    }

    private Meal meal(String id, String userId) {
        Meal meal = new Meal();
        meal.setId(id);
        meal.setUserId(userId);
        meal.setMealDate(LocalDate.of(2026, 5, 22));
        meal.setMealType("lunch");
        meal.setMemo("점심 메모");
        meal.setFoods(List.of(food("food_oat", "오트밀", 180.0)));
        meal.setCreatedAt(LocalDateTime.of(2026, 5, 22, 12, 0));
        meal.setUpdatedAt(LocalDateTime.of(2026, 5, 22, 12, 30));
        return meal;
    }

    private FoodItem food(String code, String name, double grams) {
        FoodItem food = new FoodItem();
        food.setCode(code);
        food.setName(name);
        food.setCategory("곡류");
        food.setGrams(grams);
        food.setEnergy(380.0);
        food.setCarbs(66.0);
        food.setProtein(13.0);
        food.setFat(7.0);
        return food;
    }

    private NutritionSummary summary() {
        NutritionSummary summary = new NutritionSummary();
        summary.setCalories(380.0);
        summary.setCarbs(66.0);
        summary.setProtein(13.0);
        summary.setFat(7.0);
        summary.setCarbsPct(69);
        summary.setProteinPct(14);
        summary.setFatPct(17);
        return summary;
    }
}
