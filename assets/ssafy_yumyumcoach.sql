-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema ssafy_yumyumcoach
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `ssafy_yumyumcoach` ;

-- -----------------------------------------------------
-- Schema ssafy_yumyumcoach
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `ssafy_yumyumcoach` DEFAULT CHARACTER SET utf8mb4 ;
USE `ssafy_yumyumcoach` ;

-- -----------------------------------------------------
-- Table `ssafy_yumyumcoach`.`users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_yumyumcoach`.`users` ;

CREATE TABLE IF NOT EXISTS `ssafy_yumyumcoach`.`users` (
  `user_id` VARCHAR(64) NOT NULL COMMENT '회원 PK',
  `email` VARCHAR(100) NOT NULL COMMENT '이메일',
  `password` VARCHAR(100) NOT NULL COMMENT '비밀번호',
  `nickname` VARCHAR(50) NOT NULL COMMENT '닉네임',
  `gender` VARCHAR(20) NULL DEFAULT NULL COMMENT '성별',
  `birth_year` INT NULL DEFAULT NULL COMMENT '출생연도',
  `height` DECIMAL(5,2) NULL DEFAULT NULL COMMENT '키(cm)',
  `weight` DECIMAL(5,2) NULL DEFAULT NULL COMMENT '몸무게(kg)',
  `goal` VARCHAR(100) NULL DEFAULT NULL COMMENT '건강 목표',
  `health_note` VARCHAR(500) NULL DEFAULT NULL COMMENT '건강 메모/질환 정보',
  `active` TINYINT(1) NOT NULL DEFAULT '1' COMMENT '활성 여부',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`user_id`),
  UNIQUE INDEX `email` (`email` ASC) VISIBLE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
COMMENT = '회원 정보 테이블';


-- -----------------------------------------------------
-- Table `ssafy_yumyumcoach`.`diet_logs`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_yumyumcoach`.`diet_logs` ;

CREATE TABLE IF NOT EXISTS `ssafy_yumyumcoach`.`diet_logs` (
  `diet_log_id` INT NOT NULL AUTO_INCREMENT COMMENT '식단로그 PK',
  `user_id` VARCHAR(64) NOT NULL COMMENT 'FK: users.user_id',
  `log_date` DATETIME NOT NULL COMMENT '식단 기록 일시',
  `meal_type` VARCHAR(20) NULL DEFAULT NULL COMMENT '식사 유형(아침/점심/저녁/간식)',
  `total_calorie` DECIMAL(10,2) NULL DEFAULT NULL COMMENT '총 칼로리(옵션)',
  `memo` VARCHAR(500) NULL DEFAULT NULL COMMENT '메모/코멘트',
  `image_url` VARCHAR(500) NULL DEFAULT NULL COMMENT '식단 이미지 URL',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`diet_log_id`),
  INDEX `idx_diet_logs_user_date` (`user_id` ASC, `log_date` ASC) VISIBLE,
  CONSTRAINT `fk_diet_logs_user_id`
    FOREIGN KEY (`user_id`)
    REFERENCES `ssafy_yumyumcoach`.`users` (`user_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 4
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
COMMENT = '식단 기록 테이블';


-- -----------------------------------------------------
-- Table `ssafy_yumyumcoach`.`food_nutrition`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_yumyumcoach`.`food_nutrition` ;

CREATE TABLE IF NOT EXISTS `ssafy_yumyumcoach`.`food_nutrition` (
  `food_code` VARCHAR(50) NOT NULL COMMENT '식품코드',
  `food_name` VARCHAR(200) NOT NULL COMMENT '식품명',
  `category` VARCHAR(100) NULL DEFAULT NULL COMMENT '식품대분류명',
  `weight` VARCHAR(50) NULL DEFAULT NULL COMMENT '식품중량(표시단위 포함)',
  `energy_kcal` DECIMAL(10,2) NULL DEFAULT NULL COMMENT '에너지(kcal)',
  `protein_g` DECIMAL(10,2) NULL DEFAULT NULL COMMENT '단백질(g)',
  `fat_g` DECIMAL(10,2) NULL DEFAULT NULL COMMENT '지방(g)',
  `carbohydrate_g` DECIMAL(10,2) NULL DEFAULT NULL COMMENT '탄수화물(g)',
  `sugar_g` DECIMAL(10,2) NULL DEFAULT NULL COMMENT '당류(g)',
  `sodium_mg` DECIMAL(10,2) NULL DEFAULT NULL COMMENT '나트륨(mg)',
  `cholesterol_mg` DECIMAL(10,2) NULL DEFAULT NULL COMMENT '콜레스테롤(mg)',
  `saturated_fat_g` DECIMAL(10,2) NULL DEFAULT NULL COMMENT '포화지방산(g)',
  `trans_fat_g` DECIMAL(10,2) NULL DEFAULT NULL COMMENT '트랜스지방산(g)',
  `caffeine_mg` DECIMAL(10,2) NULL DEFAULT NULL COMMENT '카페인(mg)',
  PRIMARY KEY (`food_code`),
  FULLTEXT INDEX `idx_ft_food_name` (`food_name`) VISIBLE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
COMMENT = '음식 영양정보 테이블';


-- -----------------------------------------------------
-- Table `ssafy_yumyumcoach`.`diet_log_items`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ssafy_yumyumcoach`.`diet_log_items` ;

CREATE TABLE IF NOT EXISTS `ssafy_yumyumcoach`.`diet_log_items` (
  `diet_log_item_id` INT NOT NULL AUTO_INCREMENT COMMENT '식단 항목 PK',
  `diet_log_id` INT NOT NULL COMMENT 'FK: diet_logs.diet_log_id',
  `food_code` VARCHAR(50) NOT NULL COMMENT 'FK: food_nutrition.food_code',
  `serving_size` DECIMAL(5,2) NOT NULL DEFAULT '1.00' COMMENT '섭취량(인분)',
  `selected_grams` DECIMAL(10,2) NULL DEFAULT NULL COMMENT '선택 섭취량(g)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`diet_log_item_id`),
  INDEX `idx_diet_log_items_diet_log_id` (`diet_log_id` ASC) VISIBLE,
  INDEX `idx_diet_log_items_food_code` (`food_code` ASC) VISIBLE,
  CONSTRAINT `fk_diet_log_items_diet_log_id`
    FOREIGN KEY (`diet_log_id`)
    REFERENCES `ssafy_yumyumcoach`.`diet_logs` (`diet_log_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_diet_log_items_food_code`
    FOREIGN KEY (`food_code`)
    REFERENCES `ssafy_yumyumcoach`.`food_nutrition` (`food_code`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 9
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
COMMENT = '식단별 섭취 음식 목록';


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
