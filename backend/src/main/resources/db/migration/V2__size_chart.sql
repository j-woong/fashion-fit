-- 5단계: 사이즈 비교. 사용자가 입력한 "사고 싶은 옷의 실측표"와 그 결과 요약을 저장한다.

CREATE TABLE size_chart (
    id                     BIGINT       NOT NULL AUTO_INCREMENT,
    user_id                BIGINT       NOT NULL,
    category               VARCHAR(30)  NOT NULL,
    product_name           VARCHAR(100) NOT NULL,
    product_url            VARCHAR(500),
    -- 비교 당시 기준 옷. FK 를 걸지 않는 이유: 기준 옷을 지워도 비교 기록은 남아야 해서 (이름은 스냅샷으로 보관)
    reference_garment_id   BIGINT,
    reference_garment_name VARCHAR(50),
    recommended_size       VARCHAR(20),
    created_at             DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_size_chart_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_size_chart_user_created (user_id, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- 실측표의 칸 하나 = (사이즈, 부위, 값). 예: (M, CHEST_WIDTH, 56.0)
-- entry_order: 사용자가 입력한 순서(S → M → L)를 그대로 보여주기 위한 순번
CREATE TABLE size_chart_entry (
    size_chart_id BIGINT      NOT NULL,
    entry_order   INT         NOT NULL,
    size_label    VARCHAR(20) NOT NULL,
    part          VARCHAR(30) NOT NULL,
    value_cm      DOUBLE      NOT NULL,
    PRIMARY KEY (size_chart_id, entry_order),
    CONSTRAINT fk_size_chart_entry_chart FOREIGN KEY (size_chart_id) REFERENCES size_chart (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
