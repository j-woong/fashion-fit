package com.jiwoong.fashionfit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import com.jiwoong.fashionfit.auth.GoogleOAuth2UserService;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   GoogleOAuth2UserService googleOAuth2UserService,
                                                   @Value("${app.frontend-url}") String frontendUrl) throws Exception {
        // CSRF: 저장/수정/삭제 요청(POST·PUT·DELETE)이 "우리 화면"에서 온 건지 확인하는 토큰.
        // 1) 서버가 XSRF-TOKEN 쿠키로 토큰을 내려주고  2) 프론트가 그 값을 X-XSRF-TOKEN 헤더에 담아 보내면  3) 서버가 둘을 비교한다.
        // 다른 사이트는 우리 쿠키 값을 읽을 수 없어서 헤더를 만들 수 없다.
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName(null); // 모든 응답에 토큰 쿠키를 바로 내려준다 (SPA용)

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) // JS가 쿠키를 읽을 수 있게
                        .csrfTokenRequestHandler(csrfHandler)
                        .ignoringRequestMatchers("/h2-console/**"))
                // H2 콘솔 화면이 iframe 을 써서 같은 출처만 허용
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health", "/h2-console/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                // API 요청인데 로그인이 안 돼 있으면 구글 로그인 페이지로 튕기지 말고 401만 돌려준다.
                // 화면 이동은 프론트(React)가 결정한다.
                .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        PathPatternRequestMatcher.withDefaults().matcher("/api/**")))
                // 구글 로그인 흐름: /oauth2/authorization/google 로 시작 → 구글 → /login/oauth2/code/google 로 돌아옴
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(googleOAuth2UserService))
                        // 로그인이 끝나면 백엔드 화면이 아니라 React 화면으로 보낸다
                        .defaultSuccessUrl(frontendUrl + "/", true)
                        .failureUrl(frontendUrl + "/?loginError=true"))
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()));

        return http.build();
    }
}
