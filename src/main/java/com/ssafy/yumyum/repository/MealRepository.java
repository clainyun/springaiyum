package com.ssafy.yumyum.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.ssafy.yumyum.model.FoodItem;
import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.util.DBUtil;

public class MealRepository {

    private static final String CREATE_MEALS_SQL = """
        CREATE TABLE IF NOT EXISTS meals (
            meal_id VARCHAR(64) NOT NULL PRIMARY KEY,
            user_id VARCHAR(64) NOT NULL,
            meal_date DATE NOT NULL,
            meal_type VARCHAR(20) NOT NULL,
            memo VARCHAR(500) NULL,
            created_at DATETIME NOT NULL,
            updated_at DATETIME NOT NULL,
            INDEX idx_meals_user_date (user_id, meal_date)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """;

    private static final String CREATE_MEAL_FOODS_SQL = """
        CREATE TABLE IF NOT EXISTS meal_foods (
            meal_id VARCHAR(64) NOT NULL,
            item_order INT NOT NULL,
            food_code VARCHAR(64) NOT NULL,
            food_name VARCHAR(200) NOT NULL,
            category VARCHAR(100) NULL,
            grams DOUBLE NOT NULL,
            energy DOUBLE NOT NULL,
            carbs DOUBLE NOT NULL,
            protein DOUBLE NOT NULL,
            fat DOUBLE NOT NULL,
            PRIMARY KEY (meal_id, item_order),
            CONSTRAINT fk_meal_foods_meal_id
                FOREIGN KEY (meal_id) REFERENCES meals (meal_id) ON DELETE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """;

    public MealRepository(List<Meal> seedMeals) {
        initializeSchema();
        seedIfEmpty(seedMeals == null ? List.of() : seedMeals);
    }

    public synchronized List<Meal> findAll() {
        String sql = """
            SELECT m.meal_id, m.user_id, m.meal_date, m.meal_type, m.memo, m.created_at, m.updated_at,
                   f.item_order, f.food_code, f.food_name, f.category, f.grams, f.energy, f.carbs, f.protein, f.fat
              FROM meals m
              LEFT JOIN meal_foods f ON m.meal_id = f.meal_id
             ORDER BY m.created_at ASC, m.meal_id ASC, f.item_order ASC
            """;
        return loadMeals(sql, null);
    }

    public synchronized Meal findById(String id) {
        String sql = """
            SELECT m.meal_id, m.user_id, m.meal_date, m.meal_type, m.memo, m.created_at, m.updated_at,
                   f.item_order, f.food_code, f.food_name, f.category, f.grams, f.energy, f.carbs, f.protein, f.fat
              FROM meals m
              LEFT JOIN meal_foods f ON m.meal_id = f.meal_id
             WHERE m.meal_id = ?
             ORDER BY f.item_order ASC
            """;
        List<Meal> meals = loadMeals(sql, id);
        return meals.isEmpty() ? null : meals.get(0);
    }

    public synchronized void save(Meal meal) {
        Objects.requireNonNull(meal, "meal must not be null");

        try (Connection connection = DBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                persistMeal(connection, meal);
                connection.commit();
            } catch (SQLException e) {
                rollbackQuietly(connection);
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("식단을 저장하지 못했습니다.", e);
        }
    }

    public synchronized void delete(String mealId) {
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement deleteFoods = connection.prepareStatement("DELETE FROM meal_foods WHERE meal_id = ?");
             PreparedStatement deleteMeal = connection.prepareStatement("DELETE FROM meals WHERE meal_id = ?")) {
            connection.setAutoCommit(false);
            try {
                deleteFoods.setString(1, mealId);
                deleteFoods.executeUpdate();

                deleteMeal.setString(1, mealId);
                deleteMeal.executeUpdate();

                connection.commit();
            } catch (SQLException e) {
                rollbackQuietly(connection);
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("식단을 삭제하지 못했습니다.", e);
        }
    }

    private void initializeSchema() {
        try (Connection connection = DBUtil.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_MEALS_SQL);
            statement.executeUpdate(CREATE_MEAL_FOODS_SQL);
        } catch (SQLException e) {
            throw new IllegalStateException("MealRepository용 테이블을 초기화하지 못했습니다.", e);
        }
    }

    private void seedIfEmpty(List<Meal> seedMeals) {
        if (seedMeals.isEmpty()) {
            return;
        }

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement countStatement = connection.prepareStatement("SELECT COUNT(*) FROM meals");
             ResultSet resultSet = countStatement.executeQuery()) {
            if (resultSet.next() && resultSet.getInt(1) > 0) {
                return;
            }

            connection.setAutoCommit(false);
            try {
                for (Meal meal : seedMeals) {
                    persistMeal(connection, meal);
                }
                connection.commit();
            } catch (SQLException e) {
                rollbackQuietly(connection);
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("초기 식단 데이터를 적재하지 못했습니다.", e);
        }
    }

    private void persistMeal(Connection connection, Meal meal) throws SQLException {
        String upsertMealSql = """
            INSERT INTO meals (meal_id, user_id, meal_date, meal_type, memo, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                user_id = VALUES(user_id),
                meal_date = VALUES(meal_date),
                meal_type = VALUES(meal_type),
                memo = VALUES(memo),
                created_at = VALUES(created_at),
                updated_at = VALUES(updated_at)
            """;
        String deleteFoodsSql = "DELETE FROM meal_foods WHERE meal_id = ?";
        String insertFoodSql = """
            INSERT INTO meal_foods (
                meal_id, item_order, food_code, food_name, category, grams, energy, carbs, protein, fat
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        LocalDateTime createdAt = meal.getCreatedAt() == null ? LocalDateTime.now() : meal.getCreatedAt();
        LocalDateTime updatedAt = meal.getUpdatedAt() == null ? createdAt : meal.getUpdatedAt();

        try (PreparedStatement upsertMeal = connection.prepareStatement(upsertMealSql);
             PreparedStatement deleteFoods = connection.prepareStatement(deleteFoodsSql);
             PreparedStatement insertFood = connection.prepareStatement(insertFoodSql)) {
            upsertMeal.setString(1, meal.getId());
            upsertMeal.setString(2, meal.getUserId());
            upsertMeal.setDate(3, Date.valueOf(meal.getMealDate()));
            upsertMeal.setString(4, meal.getMealType());
            upsertMeal.setString(5, meal.getMemo());
            upsertMeal.setTimestamp(6, Timestamp.valueOf(createdAt));
            upsertMeal.setTimestamp(7, Timestamp.valueOf(updatedAt));
            upsertMeal.executeUpdate();

            deleteFoods.setString(1, meal.getId());
            deleteFoods.executeUpdate();

            List<FoodItem> foods = meal.getFoods() == null ? List.of() : meal.getFoods();
            for (int index = 0; index < foods.size(); index++) {
                FoodItem food = foods.get(index);
                insertFood.setString(1, meal.getId());
                insertFood.setInt(2, index);
                insertFood.setString(3, food.getCode());
                insertFood.setString(4, food.getName());
                insertFood.setString(5, food.getCategory());
                insertFood.setDouble(6, food.getGrams());
                insertFood.setDouble(7, food.getEnergy());
                insertFood.setDouble(8, food.getCarbs());
                insertFood.setDouble(9, food.getProtein());
                insertFood.setDouble(10, food.getFat());
                insertFood.addBatch();
            }
            insertFood.executeBatch();
        }
    }

    private List<Meal> loadMeals(String sql, String mealId) {
        Map<String, Meal> meals = new LinkedHashMap<>();

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (mealId != null) {
                statement.setString(1, mealId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String currentMealId = resultSet.getString("meal_id");
                    Meal meal = meals.get(currentMealId);
                    if (meal == null) {
                        meal = mapMeal(resultSet);
                        meals.put(currentMealId, meal);
                    }

                    String foodCode = resultSet.getString("food_code");
                    if (foodCode != null) {
                        meal.getFoods().add(mapFood(resultSet));
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("식단을 조회하지 못했습니다.", e);
        }

        return new ArrayList<>(meals.values());
    }

    private Meal mapMeal(ResultSet resultSet) throws SQLException {
        Meal meal = new Meal();
        meal.setId(resultSet.getString("meal_id"));
        meal.setUserId(resultSet.getString("user_id"));

        Date mealDate = resultSet.getDate("meal_date");
        meal.setMealDate(mealDate == null ? null : mealDate.toLocalDate());

        meal.setMealType(resultSet.getString("meal_type"));
        meal.setMemo(resultSet.getString("memo"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        meal.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        meal.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());
        return meal;
    }

    private FoodItem mapFood(ResultSet resultSet) throws SQLException {
        FoodItem food = new FoodItem();
        food.setCode(resultSet.getString("food_code"));
        food.setName(resultSet.getString("food_name"));
        food.setCategory(resultSet.getString("category"));
        food.setGrams(resultSet.getDouble("grams"));
        food.setEnergy(resultSet.getDouble("energy"));
        food.setCarbs(resultSet.getDouble("carbs"));
        food.setProtein(resultSet.getDouble("protein"));
        food.setFat(resultSet.getDouble("fat"));
        return food;
    }

    private void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }
}
