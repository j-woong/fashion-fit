package com.jiwoong.fashionfit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.jiwoong.fashionfit.support.IntegrationTestSupport;

// 이 테스트가 통과한다 = ① Flyway SQL 이 MySQL 에서 오류 없이 실행되고
//                     ② Hibernate(ddl-auto: validate)가 엔티티와 테이블이 일치한다고 판단했다
class SchemaMigrationTest extends IntegrationTestSupport {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void Flyway_마이그레이션이_적용되고_엔티티와_스키마가_일치한다() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()", String.class);

        assertThat(tables).contains("users", "body_profile", "reference_garment", "garment_measurement",
                "flyway_schema_history");
    }
}
