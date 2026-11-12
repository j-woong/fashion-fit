package com.jiwoong.fashionfit.bodyprofile;

import java.time.LocalDateTime;

import com.jiwoong.fashionfit.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// ERD의 body_profile 테이블. 회원 한 명당 하나(1:1).
// 발 사이즈만 필수, 키는 권장, 몸무게는 선택(민감한 정보라 최소한으로 받는다).
@Entity
@Table(name = "body_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BodyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private Integer heightCm;

    private Integer weightKg;

    @Column(nullable = false)
    private Integer footSizeMm;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private BodyProfile(User user, Integer heightCm, Integer weightKg, Integer footSizeMm) {
        this.user = user;
        update(heightCm, weightKg, footSizeMm);
    }

    public static BodyProfile create(User user, Integer heightCm, Integer weightKg, Integer footSizeMm) {
        return new BodyProfile(user, heightCm, weightKg, footSizeMm);
    }

    public void update(Integer heightCm, Integer weightKg, Integer footSizeMm) {
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.footSizeMm = footSizeMm;
        this.updatedAt = LocalDateTime.now();
    }
}
