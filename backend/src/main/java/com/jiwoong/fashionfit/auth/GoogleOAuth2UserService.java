package com.jiwoong.fashionfit.auth;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jiwoong.fashionfit.user.Provider;
import com.jiwoong.fashionfit.user.User;
import com.jiwoong.fashionfit.user.UserRepository;

import lombok.RequiredArgsConstructor;

// 구글 로그인이 끝나면 Spring Security가 이 클래스를 부른다.
// 1) 구글에서 사용자 정보를 받아오고  2) 처음이면 회원가입, 아니면 기존 회원을 찾아서  3) LoginUser 로 바꿔 돌려준다.
@Service
@RequiredArgsConstructor
public class GoogleOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User googleUser = super.loadUser(request);

        String providerId = googleUser.getAttribute("sub");
        String email = googleUser.getAttribute("email");
        String name = googleUser.getAttribute("name");
        String nickname = (name != null && !name.isBlank()) ? name : email;

        User user = userRepository.findByProviderAndProviderId(Provider.GOOGLE, providerId)
                .map(found -> {
                    found.syncEmail(email);
                    return found;
                })
                .orElseGet(() -> userRepository.save(
                        User.signUp(Provider.GOOGLE, providerId, email, nickname)));

        return new LoginUser(user.getId(), user.getEmail(), user.getNickname(), googleUser.getAttributes());
    }
}
