-- =====================================================
-- YumYumCoach DB Schema
-- 강의 제공 스펙 기준 + 회원 필수 기능 추가
-- =====================================================

-- [0] 스키마 생성
DROP DATABASE IF EXISTS ssafy_yumyumcoach;
CREATE DATABASE IF NOT EXISTS ssafy_yumyumcoach
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE ssafy_yumyumcoach;


-- ------------------------------
-- 1. users (회원 정보)
-- F106 회원 작성
-- F107 회원 조회
-- F108 회원 수정
-- F109 회원 삭제/비활성화
-- F110 로그인/로그아웃
-- ------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id      INT AUTO_INCREMENT PRIMARY KEY COMMENT '회원 PK',
    email        VARCHAR(100) NOT NULL UNIQUE COMMENT '이메일',
    password     VARCHAR(100) NOT NULL COMMENT '비밀번호',
    nickname     VARCHAR(50)  NOT NULL COMMENT '닉네임',
    gender       VARCHAR(20)  NULL COMMENT '성별',
    birth_year   INT          NULL COMMENT '출생연도',
    height       DECIMAL(5,2) NULL COMMENT '키(cm)',
    weight       DECIMAL(5,2) NULL COMMENT '몸무게(kg)',
    goal         VARCHAR(100) NULL COMMENT '건강 목표',
    health_note  VARCHAR(500) NULL COMMENT '건강 메모/질환 정보',
    active       BOOLEAN      NOT NULL DEFAULT TRUE COMMENT '활성 여부',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                 ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='회원 정보 테이블';


-- ------------------------------
-- 2. food_nutrition (음식 영양정보)
-- 강의 제공 스키마 유지
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
    FULLTEXT KEY idx_ft_food_name (food_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='음식 영양정보 테이블';


-- ------------------------------
-- 3. diet_logs (식단 기록 헤더)
-- 강의 제공 스키마 기준 + users FK + meal_type 추가
-- ------------------------------
CREATE TABLE IF NOT EXISTS diet_logs (
    diet_log_id   INT AUTO_INCREMENT PRIMARY KEY COMMENT '식단로그 PK',
    user_id       INT NOT NULL COMMENT 'FK: users.user_id',
    log_date      DATETIME NOT NULL COMMENT '식단 기록 일시',
    meal_type     VARCHAR(20) NULL COMMENT '식사 유형(아침/점심/저녁/간식)',
    total_calorie DECIMAL(10,2) NULL COMMENT '총 칼로리(옵션)',
    memo          VARCHAR(500)  NULL COMMENT '메모/코멘트',
    image_url     VARCHAR(500)  NULL COMMENT '식단 이미지 URL',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

    CONSTRAINT fk_diet_logs_user_id
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    INDEX idx_diet_logs_user_date (user_id, log_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='식단 기록 테이블';


-- ------------------------------
-- 4. diet_log_items (식단 상세 항목)
-- 강의 제공 스키마 기준 + selected_grams 추가
-- ------------------------------
CREATE TABLE IF NOT EXISTS diet_log_items (
    diet_log_item_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '식단 항목 PK',
    diet_log_id      INT NOT NULL COMMENT 'FK: diet_logs.diet_log_id',
    food_code        VARCHAR(50) NOT NULL COMMENT 'FK: food_nutrition.food_code',
    serving_size     DECIMAL(5,2) NOT NULL DEFAULT 1.0 COMMENT '섭취량(인분)',
    selected_grams   DECIMAL(10,2) NULL COMMENT '선택 섭취량(g)',
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
        ON UPDATE CASCADE,

    INDEX idx_diet_log_items_diet_log_id (diet_log_id),
    INDEX idx_diet_log_items_food_code (food_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='식단별 섭취 음식 목록';


-- =====================================================
-- 더미 데이터
-- 실제 음식 대량 데이터는 SSAFY_COACH_Dump.sql로 food_nutrition에 대량 삽입 가능
-- 아래 음식 데이터는 덤프 실행 전 테스트용 최소 데이터
-- =====================================================

INSERT INTO users (
    user_id, email, password, nickname, gender, birth_year,
    height, weight, goal, health_note, active
) VALUES
(1, 'demo@yumyam.com', 'Demo1234!', '데모 사용자', 'FEMALE', 1999, 165.0, 55.0, 'health', '단백질 섭취를 늘리고 싶음', TRUE),
(2, 'mina@yumyam.com', '1234', '민아', 'FEMALE', 1998, 162.0, 54.0, 'diet', '저녁 식단 관리 필요', TRUE),
(3, 'joon@yumyam.com', '1234', '준호', 'MALE', 1997, 176.0, 72.0, 'muscle', '단백질 목표 달성', TRUE)
ON DUPLICATE KEY UPDATE
    email = VALUES(email),
    password = VALUES(password),
    nickname = VALUES(nickname),
    gender = VALUES(gender),
    birth_year = VALUES(birth_year),
    height = VALUES(height),
    weight = VALUES(weight),
    goal = VALUES(goal),
    health_note = VALUES(health_note),
    active = VALUES(active);


INSERT INTO food_nutrition (
    food_code, food_name, category, weight,
    energy_kcal, protein_g, fat_g, carbohydrate_g,
    sugar_g, sodium_mg, cholesterol_mg, saturated_fat_g, trans_fat_g, caffeine_mg
) VALUES
('F001', '현미밥', '탄수화물', '100g', 152.00, 3.00, 1.00, 32.00, 0.00, 5.00, 0.00, 0.20, 0.00, 0.00),
('F002', '닭가슴살', '단백질', '100g', 165.00, 31.00, 3.60, 0.00, 0.00, 74.00, 85.00, 1.00, 0.00, 0.00),
('F003', '삶은 계란', '단백질', '100g', 155.00, 13.00, 11.00, 1.10, 1.10, 124.00, 373.00, 3.30, 0.00, 0.00),
('F004', '샐러드', '채소', '100g', 35.00, 2.00, 0.50, 6.00, 2.00, 20.00, 0.00, 0.10, 0.00, 0.00),
('F005', '바나나', '과일', '100g', 89.00, 1.10, 0.30, 23.00, 12.00, 1.00, 0.00, 0.10, 0.00, 0.00),
('F006', '연어구이', '단백질', '100g', 206.00, 22.00, 12.00, 0.00, 0.00, 59.00, 63.00, 2.50, 0.00, 0.00),
('F007', '고구마', '탄수화물', '100g', 128.00, 1.40, 0.20, 30.00, 6.00, 15.00, 0.00, 0.10, 0.00, 0.00)
ON DUPLICATE KEY UPDATE
    food_name = VALUES(food_name),
    category = VALUES(category),
    weight = VALUES(weight),
    energy_kcal = VALUES(energy_kcal),
    protein_g = VALUES(protein_g),
    fat_g = VALUES(fat_g),
    carbohydrate_g = VALUES(carbohydrate_g),
    sugar_g = VALUES(sugar_g),
    sodium_mg = VALUES(sodium_mg),
    cholesterol_mg = VALUES(cholesterol_mg),
    saturated_fat_g = VALUES(saturated_fat_g),
    trans_fat_g = VALUES(trans_fat_g),
    caffeine_mg = VALUES(caffeine_mg);


INSERT INTO diet_logs (
    diet_log_id, user_id, log_date, meal_type, total_calorie, memo, image_url
) VALUES
(1, 1, '2026-04-24 08:00:00', '아침', 472.00, '오전에 포만감을 유지하는 아침 식단입니다.', NULL),
(2, 1, '2026-04-24 12:30:00', '점심', 461.00, '채소와 단백질을 같이 챙긴 점심입니다.', NULL),
(3, 1, '2026-04-23 18:30:00', '저녁', 200.00, '회복을 고려한 가벼운 저녁입니다.', NULL)
ON DUPLICATE KEY UPDATE
    user_id = VALUES(user_id),
    log_date = VALUES(log_date),
    meal_type = VALUES(meal_type),
    total_calorie = VALUES(total_calorie),
    memo = VALUES(memo),
    image_url = VALUES(image_url);


INSERT INTO diet_log_items (
    diet_log_item_id, diet_log_id, food_code, serving_size, selected_grams
) VALUES
(1, 1, 'F001', 1.5, 150.00),
(2, 1, 'F003', 1.0, 100.00),
(3, 1, 'F005', 1.0, 100.00),

(4, 2, 'F001', 1.5, 150.00),
(5, 2, 'F002', 1.2, 120.00),
(6, 2, 'F004', 1.0, 100.00),

(7, 3, 'F006', 1.0, 100.00),
(8, 3, 'F004', 1.0, 100.00)
ON DUPLICATE KEY UPDATE
    diet_log_id = VALUES(diet_log_id),
    food_code = VALUES(food_code),
    serving_size = VALUES(serving_size),
    selected_grams = VALUES(selected_grams);