package com.ssafy.yumyum.tool;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.ssafy.yumyum.model.FoodItem;
import com.ssafy.yumyum.repository.FoodCatalogRepository;

@Component
public class FoodSearchTool {

    private static final int MAX_RESULTS = 5;

    private final FoodCatalogRepository foodCatalogRepository;

    public FoodSearchTool(FoodCatalogRepository foodCatalogRepository) {
        this.foodCatalogRepository = foodCatalogRepository;
    }

    @Tool(description = "음식 이름 키워드로 영양성분(칼로리, 탄수화물, 단백질, 지방)을 검색합니다. 100g 기준 영양 데이터를 반환합니다.")
    public List<FoodItem> searchFood(
            @ToolParam(description = "검색할 음식 이름 키워드 (예: 닭가슴살, 현미밥, 고구마)") String keyword) {
        return foodCatalogRepository.search(keyword)
                .stream()
                .limit(MAX_RESULTS)
                .toList();
    }
}
