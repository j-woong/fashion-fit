package com.jiwoong.fashionfit.user;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// ERD의 users 테이블. 같은 소셜 계정(provider + providerId)은 한 번만 가입된다.
@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Hibernate 6+ 는 MySQL 에서 enum 필드를 MySQL 전용 ENUM 타입 컬럼으로 기대한다.
    // 우리는 값 추가가 쉬운 VARCHAR 로 저장하므로 명시한다 (안 하면 ddl-auto: validate 에서 타입 불일치로 서버가 안 뜸).
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private Provider provider;

    // 구글이 주는 사용자 고유 번호(sub). 이메일은 바뀔 수 있어서 이걸로 사람을 구분한다.
    @Column(name = "provider_id", nullable = false)
    private String providerId;

    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private User(Provider provider, String providerId, String email, String nickname) {
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.nickname = nickname;
        this.createdAt = LocalDateTime.now();
    }

    public static User signUp(Provider provider, String providerId, String email, String nickname) {
        return new User(provider, providerId, email, nickname);
    }

    // 로그인할 때마다 구글 쪽 최신 이메일로 맞춘다. (닉네임은 사용자가 앱에서 바꿀 수 있게 건드리지 않음)
    public void syncEmail(String email) {
        this.email = email;
    }
}
