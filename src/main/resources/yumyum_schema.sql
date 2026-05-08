DROP DATABASE IF EXISTS yamyam_db;
CREATE DATABASE yamyam_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE yamyam_db;

CREATE TABLE users (
    user_id VARCHAR(50) PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    gender VARCHAR(20),
    birth_year INT,
    height DOUBLE,
    weight DOUBLE,
    goal VARCHAR(100),
    health_note VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE foods (
    food_code VARCHAR(50) PRIMARY KEY,
    food_name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    base_grams DOUBLE NOT NULL DEFAULT 100,
    energy DOUBLE NOT NULL DEFAULT 0,
    carbs DOUBLE NOT NULL DEFAULT 0,
    protein DOUBLE NOT NULL DEFAULT 0,
    fat DOUBLE NOT NULL DEFAULT 0
);

CREATE TABLE meals (
    meal_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    meal_date DATE NOT NULL,
    meal_type VARCHAR(20) NOT NULL,
    memo VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_meals_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE meal_foods (
    meal_id VARCHAR(50) NOT NULL,
    food_code VARCHAR(50) NOT NULL,
    selected_grams DOUBLE NOT NULL DEFAULT 100,
    energy DOUBLE NOT NULL DEFAULT 0,
    carbs DOUBLE NOT NULL DEFAULT 0,
    protein DOUBLE NOT NULL DEFAULT 0,
    fat DOUBLE NOT NULL DEFAULT 0,
    PRIMARY KEY (meal_id, food_code),
    CONSTRAINT fk_meal_foods_meal
        FOREIGN KEY (meal_id) REFERENCES meals(meal_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_meal_foods_food
        FOREIGN KEY (food_code) REFERENCES foods(food_code)
);

CREATE INDEX idx_meals_user_date ON meals(user_id, meal_date);
CREATE INDEX idx_foods_name ON foods(food_name);

INSERT INTO users (
    user_id, email, password, nickname, gender, birth_year,
    height, weight, goal, health_note, active
) VALUES
('U001', 'demo@yamyam.com', 'Demo1234!', '데모 사용자', 'FEMALE', 1999, 165.0, 55.0, '건강 유지', '단백질 섭취를 늘리고 싶음', TRUE),
('U002', 'mina@yamyam.com', '1234', '민아', 'FEMALE', 1998, 162.0, 54.0, '다이어트', '저녁 식단 관리 필요', TRUE),
('U003', 'joon@yamyam.com', '1234', '준호', 'MALE', 1997, 176.0, 72.0, '근육', '단백질 목표 달성', TRUE);

INSERT INTO foods (
    food_code, food_name, category, base_grams, energy, carbs, protein, fat
) VALUES
('F001', '현미밥', '탄수화물', 100, 152, 32.0, 3.0, 1.0),
('F002', '닭가슴살', '단백질', 100, 165, 0.0, 31.0, 3.6),
('F003', '삶은 계란', '단백질', 100, 155, 1.1, 13.0, 11.0),
('F004', '샐러드', '채소', 100, 35, 6.0, 2.0, 0.5),
('F005', '바나나', '과일', 100, 89, 23.0, 1.1, 0.3),
('F006', '연어구이', '단백질', 100, 206, 0.0, 22.0, 12.0),
('F007', '고구마', '탄수화물', 100, 128, 30.0, 1.4, 0.2);

INSERT INTO meals (
    meal_id, user_id, meal_date, meal_type, memo
) VALUES
('M001', 'U001', '2026-04-24', '아침', '오전에 포만감을 유지하는 아침 식단입니다.'),
('M002', 'U001', '2026-04-24', '점심', '채소와 단백질을 같이 챙긴 점심입니다.'),
('M003', 'U001', '2026-04-23', '저녁', '회복을 고려한 가벼운 저녁입니다.');

INSERT INTO meal_foods (
    meal_id, food_code, selected_grams, energy, carbs, protein, fat
) VALUES
('M001', 'F001', 150, 228.0, 48.0, 4.5, 1.5),
('M001', 'F003', 100, 155.0, 1.1, 13.0, 11.0),
('M001', 'F005', 100, 89.0, 23.0, 1.1, 0.3),

('M002', 'F001', 150, 228.0, 48.0, 4.5, 1.5),
('M002', 'F002', 120, 198.0, 0.0, 37.2, 4.3),
('M002', 'F004', 100, 35.0, 6.0, 2.0, 0.5),

('M003', 'F006', 120, 247.2, 0.0, 26.4, 14.4),
('M003', 'F004', 100, 35.0, 6.0, 2.0, 0.5);