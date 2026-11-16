package com.jiwoong.fashionfit.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// DB까지 붙는 통합 테스트의 공통 부모.
// 모든 통합 테스트가 같은 설정을 쓰면 스프링이 컨텍스트(와 MySQL 컨테이너)를 한 번만 띄우고 재사용해서 빠르다.
@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {
}
