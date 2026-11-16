package com.jiwoong.fashionfit.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.jiwoong.fashionfit.support.IntegrationTestSupport;

// @Transactional: 테스트가 끝나면 롤백돼서 다른 테스트에 데이터가 남지 않는다
@Transactional
class UserRepositoryTest extends IntegrationTestSupport {

    @Autowired
    UserRepository userRepository;

    @Test
    void 성공_소셜_계정으로_가입한_회원을_provider와_providerId로_찾는다() {
        userRepository.saveAndFlush(User.signUp(Provider.GOOGLE, "google-123", "a@test.com", "지웅"));

        assertThat(userRepository.findByProviderAndProviderId(Provider.GOOGLE, "google-123"))
                .get()
                .extracting(User::getEmail)
                .isEqualTo("a@test.com");
    }

    @Test
    void 실패_같은_소셜_계정은_두_번_가입할_수_없다() {
        userRepository.saveAndFlush(User.signUp(Provider.GOOGLE, "google-123", "a@test.com", "지웅"));

        // DB 의 UNIQUE(provider, provider_id) 제약이 막아 준다 (V1__init.sql)
        assertThatThrownBy(() -> userRepository.saveAndFlush(
                User.signUp(Provider.GOOGLE, "google-123", "other@test.com", "다른사람")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
