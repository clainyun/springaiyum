package com.ssafy.yumyum.repository;

import com.ssafy.yumyum.model.FoodItem;
import com.ssafy.yumyum.util.DBUtil;

import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class FoodCatalogRepository {

    private static final String BASE_SELECT = """
        SELECT food_code, food_name, category, weight, energy_kcal, carbohydrate_g, protein_g, fat_g
        FROM food_nutrition
        """;

    private static final String FIND_ALL_SQL = BASE_SELECT + " ORDER BY food_name, food_code";

    private static final String FIND_BY_CODE_SQL = BASE_SELECT + " WHERE food_code = ?";

    private static final String SEARCH_SQL = BASE_SELECT
            + " WHERE food_name LIKE ? OR category LIKE ? ORDER BY food_name, food_code";

    private static final Pattern WEIGHT_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)");

    public List<FoodItem> findAll() {
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            return mapFoods(resultSet);

        } catch (SQLException e) {
            throw repositoryException("find all foods", e);
        }
    }

    public FoodItem findByCode(String code) {
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_CODE_SQL)) {

            statement.setString(1, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapFood(resultSet);
                }
                return null;
            }

        } catch (SQLException e) {
            throw repositoryException("find food by code", e);
        }
    }

    public List<FoodItem> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }

        String searchKeyword = "%" + keyword.trim() + "%";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SEARCH_SQL)) {

            statement.setString(1, searchKeyword);
            statement.setString(2, searchKeyword);

            try (ResultSet resultSet = statement.executeQuery()) {
                return mapFoods(resultSet);
            }

        } catch (SQLException e) {
            throw repositoryException("search foods", e);
        }
    }

    private List<FoodItem> mapFoods(ResultSet resultSet) throws SQLException {
        List<FoodItem> foods = new ArrayList<>();

        while (resultSet.next()) {
            foods.add(mapFood(resultSet));
        }

        return foods;
    }

    private FoodItem mapFood(ResultSet resultSet) throws SQLException {
        double servingGrams = extractServingGrams(resultSet.getString("weight"));
        double normalizationRatio = 100.0 / servingGrams;

        FoodItem item = new FoodItem();
        item.setCode(resultSet.getString("food_code"));
        item.setName(resultSet.getString("food_name"));
        item.setCategory(defaultIfNull(resultSet.getString("category")));
        item.setGrams(100.0);
        item.setEnergy(round(resultSet.getDouble("energy_kcal") * normalizationRatio));
        item.setCarbs(round(resultSet.getDouble("carbohydrate_g") * normalizationRatio));
        item.setProtein(round(resultSet.getDouble("protein_g") * normalizationRatio));
        item.setFat(round(resultSet.getDouble("fat_g") * normalizationRatio));

        return item;
    }

    private double extractServingGrams(String weight) {
        if (weight == null || weight.isBlank()) {
            return 100.0;
        }

        Matcher matcher = WEIGHT_PATTERN.matcher(weight);

        if (!matcher.find()) {
            return 100.0;
        }

        try {
            double grams = Double.parseDouble(matcher.group(1));
            return grams > 0 ? grams : 100.0;
        } catch (NumberFormatException e) {
            return 100.0;
        }
    }

    private String defaultIfNull(String value) {
        return value == null ? "" : value;
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private IllegalStateException repositoryException(String action, SQLException cause) {
        return new IllegalStateException("Failed to " + action + ".", cause);
    }
}