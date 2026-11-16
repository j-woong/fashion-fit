-- 1~4단계에서 만든 테이블. (H2 + ddl-auto 로 자동 생성되던 것을 SQL 로 고정)
-- 규칙: 한 번 적용된 V 파일은 절대 수정하지 않는다. 바꿀 게 생기면 V2__... 파일을 새로 만든다.

CREATE TABLE users (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    provider    VARCHAR(20)  NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    email       VARCHAR(255),
    nickname    VARCHAR(255) NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_provider_provider_id UNIQUE (provider, provider_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE body_profile (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    user_id      BIGINT      NOT NULL,
    height_cm    INT,
    weight_kg    INT,
    foot_size_mm INT         NOT NULL,
    updated_at   DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_body_profile_user UNIQUE (user_id),
    CONSTRAINT fk_body_profile_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE reference_garment (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    category   VARCHAR(30) NOT NULL,
    name       VARCHAR(50) NOT NULL,
    fit_feel   VARCHAR(20) NOT NULL,
    source     VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_reference_garment_user FOREIGN KEY (user_id) REFERENCES users (id),
    -- "내 옷 목록, 최근 등록순" 조회(findAllByUserIdOrderByCreatedAtDesc)용 인덱스
    INDEX idx_reference_garment_user_created (user_id, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE garment_measurement (
    id                   BIGINT      NOT NULL AUTO_INCREMENT,
    reference_garment_id BIGINT      NOT NULL,
    part                 VARCHAR(30) NOT NULL,
    value_cm             DOUBLE      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_garment_measurement_part UNIQUE (reference_garment_id, part),
    CONSTRAINT fk_garment_measurement_garment FOREIGN KEY (reference_garment_id) REFERENCES reference_garment (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
