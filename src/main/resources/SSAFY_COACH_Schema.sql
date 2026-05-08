-- [0] 스키마 생성(옵션)
DROP DATABASE IF EXISTS ssafy_yumyumcoach;
CREATE DATABASE IF NOT EXISTS ssafy_yumyumcoach;
USE ssafy_yumyumcoach;

-- ------------------------------
-- 1. food_nutrition (음식 영양정보)
-- ------------------------------
CREATE TABLE IF NOT EXISTS food_nutrition (
    food_code         VARCHAR(50)   NOT NULL COMMENT '식품코드',
    food_name         VARCHAR(200)  NOT NULL COMMENT '식품명',
    category          VARCHAR(100)  NULL COMMENT '식품대분류명',
    weight            VARCHAR(50)   NULL COMMENT '식품중량(표시단위 포함)',
    energy_kcal       DECIMAL(10,2) NULL COMMENT '에너지(kcal)',
    protein_g         DECIMAL(10,2) NULL COMMENT '단백질(g)',
    fat_g             DECIMAL(10,2) NULL COMMENT '지방(g)',
    carbohydrate_g    DECIMAL(10,2) NULL COMMENT '탄수화물(g)',
    sugar_g           DECIMAL(10,2) NULL COMMENT '당류(g)',
    sodium_mg         DECIMAL(10,2) NULL COMMENT '나트륨(mg)',
    cholesterol_mg    DECIMAL(10,2) NULL COMMENT '콜레스테롤(mg)',
    saturated_fat_g   DECIMAL(10,2) NULL COMMENT '포화지방산(g)',
    trans_fat_g       DECIMAL(10,2) NULL COMMENT '트랜스지방산(g)',
    caffeine_mg       DECIMAL(10,2) NULL COMMENT '카페인(mg)',
    PRIMARY KEY (food_code),
	FULLTEXT KEY idx_ft_food_name (food_name) -- FULLTEXT 인덱스 추가
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='음식 영양정보 테이블';

-- ------------------------------
-- 2. diet_logs (식단 기록 헤더)
-- ------------------------------
CREATE TABLE IF NOT EXISTS diet_logs (
    diet_log_id   INT AUTO_INCREMENT PRIMARY KEY COMMENT '식단로그 PK',
    user_id       INT NOT NULL COMMENT '사용자 ID',
    log_date      DATETIME NOT NULL COMMENT '식단 기록 일시',
    total_calorie DECIMAL(10,2) NULL COMMENT '총 칼로리(옵션)',
    memo          VARCHAR(500)  NULL COMMENT '메모/코멘트',
    image_url     VARCHAR(500)  NULL COMMENT '식단 이미지 URL',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP 
                  ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='식단 기록 테이블';

-- ------------------------------
-- 3. diet_log_items (식단 상세 항목)
-- ------------------------------
CREATE TABLE IF NOT EXISTS diet_log_items (
    diet_log_item_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '식단 항목 PK',
    diet_log_id      INT NOT NULL COMMENT 'FK: diet_logs.diet_log_id',
    food_code        VARCHAR(50) NOT NULL COMMENT 'FK: food_nutrition.food_code',
    serving_size     DECIMAL(5,2) NOT NULL DEFAULT 1.0 COMMENT '섭취량(인분)',
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP 
                     ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    CONSTRAINT fk_diet_log_items_diet_log_id
        FOREIGN KEY (diet_log_id) REFERENCES diet_logs (diet_log_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_diet_log_items_food_code
        FOREIGN KEY (food_code) REFERENCES food_nutrition (food_code)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='식단별 섭취 음식 목록';

