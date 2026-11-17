package com.jiwoong.fashionfit.sizecheck;

import static com.jiwoong.fashionfit.garment.MeasurementPart.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.jiwoong.fashionfit.common.InvalidInputException;
import com.jiwoong.fashionfit.common.NotFoundException;
import com.jiwoong.fashionfit.garment.FitFeel;
import com.jiwoong.fashionfit.garment.GarmentCategory;
import com.jiwoong.fashionfit.garment.MeasurementPart;
import com.jiwoong.fashionfit.garment.ReferenceGarment;
import com.jiwoong.fashionfit.garment.ReferenceGarmentRepository;
import com.jiwoong.fashionfit.sizecheck.SizeCheckRequest.SizeInput;
import com.jiwoong.fashionfit.support.IntegrationTestSupport;
import com.jiwoong.fashionfit.user.Provider;
import com.jiwoong.fashionfit.user.User;
import com.jiwoong.fashionfit.user.UserRepository;

@Transactional
class SizeCheckServiceTest extends IntegrationTestSupport {

    @Autowired
    SizeCheckService sizeCheckService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ReferenceGarmentRepository garmentRepository;

    User me;
    User other;

    @BeforeEach
    void setUp() {
        me = userRepository.save(User.signUp(Provider.GOOGLE, "me", "me@test.com", "나"));
        other = userRepository.save(User.signUp(Provider.GOOGLE, "other", "other@test.com", "남"));
    }

    @Test
    void 성공_반팔_기준_옷만_있어도_긴팔_상품과_비교한다() {
        saveGarment(me, GarmentCategory.SHORT_SLEEVE, "흰 반팔");

        SizeCheckResponse response = sizeCheckService.create(me.getId(), request(GarmentCategory.LONG_SLEEVE,
                new SizeInput("M", Map.of(CHEST_WIDTH, 55.0, SHOULDER_WIDTH, 47.0)),
                new SizeInput("L", Map.of(CHEST_WIDTH, 58.0, SHOULDER_WIDTH, 50.0))));

        assertThat(response.reference().name()).isEqualTo("흰 반팔");
        assertThat(response.recommendedSize()).isEqualTo("L");
        assertThat(sizeCheckService.list(me.getId()))
                .singleElement()
                .extracting(SizeCheckSummary::recommendedSize)
                .isEqualTo("L");
    }

    @Test
    void 성공_기준_옷이_없어도_실측표는_저장되고_비교_불가로_응답한다() {
        SizeCheckResponse response = sizeCheckService.create(me.getId(), request(GarmentCategory.PANTS,
                new SizeInput("30", Map.of(WAIST_WIDTH, 40.0))));

        assertThat(response.verdict()).isEqualTo(Verdict.NO_REFERENCE);
        assertThat(response.id()).isNotNull();
    }

    @Test
    void 실패_사이즈_이름이_겹치면_저장하지_않는다() {
        assertThatThrownBy(() -> sizeCheckService.create(me.getId(), request(GarmentCategory.LONG_SLEEVE,
                new SizeInput("M", Map.of(CHEST_WIDTH, 55.0)),
                new SizeInput("m", Map.of(CHEST_WIDTH, 56.0)))))
                .isInstanceOfSatisfying(InvalidInputException.class,
                        e -> assertThat(e.getFieldErrors()).containsKey("sizes[1].label"));
    }

    @Test
    void 실패_카테고리에_없는_부위는_받지_않는다() {
        assertThatThrownBy(() -> sizeCheckService.create(me.getId(), request(GarmentCategory.LONG_SLEEVE,
                new SizeInput("M", Map.of(WAIST_WIDTH, 40.0)))))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 실패_바지_기준_옷으로_상의를_비교할_수_없다() {
        ReferenceGarment pants = saveGarment(me, GarmentCategory.PANTS, "청바지");
        SizeCheckRequest request = new SizeCheckRequest(GarmentCategory.LONG_SLEEVE, "맨투맨", null, pants.getId(),
                List.of(new SizeInput("M", Map.of(CHEST_WIDTH, 55.0))));

        assertThatThrownBy(() -> sizeCheckService.create(me.getId(), request))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 실패_남의_비교_기록은_볼_수_없다() {
        SizeCheckResponse mine = sizeCheckService.create(me.getId(), request(GarmentCategory.PANTS,
                new SizeInput("30", Map.of(WAIST_WIDTH, 40.0))));

        assertThatThrownBy(() -> sizeCheckService.get(other.getId(), mine.id()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 성공_같은_상품_링크면_추적_꼬리표가_달라도_저장해_둔_실측표를_찾는다() {
        sizeCheckService.create(me.getId(), new SizeCheckRequest(GarmentCategory.OUTER, "경량패딩",
                "https://shop.com/goods/55?utm_source=ig", null,
                List.of(new SizeInput("M", Map.of(CHEST_WIDTH, 58.0, TOTAL_LENGTH, 68.0)))));

        assertThat(sizeCheckService.findSavedChart(me.getId(), "https://shop.com/goods/55?fbclid=abc"))
                .hasValueSatisfying(saved -> {
                    assertThat(saved.productName()).isEqualTo("경량패딩");
                    assertThat(saved.sizes()).singleElement()
                            .satisfies(row -> assertThat(row.measurements()).containsEntry(CHEST_WIDTH, 58.0));
                });
    }

    @Test
    void 실패_남의_실측표나_쇼핑몰_첫_화면_주소로는_찾지_않는다() {
        sizeCheckService.create(other.getId(), new SizeCheckRequest(GarmentCategory.OUTER, "남의 옷",
                "https://shop.com/goods/55", null, List.of(new SizeInput("M", Map.of(CHEST_WIDTH, 58.0)))));
        sizeCheckService.create(me.getId(), new SizeCheckRequest(GarmentCategory.OUTER, "첫 화면 링크",
                "https://shop.com/?utm_source=ig", null, List.of(new SizeInput("M", Map.of(CHEST_WIDTH, 58.0)))));

        assertThat(sizeCheckService.findSavedChart(me.getId(), "https://shop.com/goods/55")).isEmpty();
        assertThat(sizeCheckService.findSavedChart(me.getId(), "https://shop.com/")).isEmpty();
    }

    private SizeCheckRequest request(GarmentCategory category, SizeInput... sizes) {
        return new SizeCheckRequest(category, "테스트 상품", null, null, List.of(sizes));
    }

    private ReferenceGarment saveGarment(User user, GarmentCategory category, String name) {
        Map<MeasurementPart, Double> values = category == GarmentCategory.PANTS
                ? Map.of(TOTAL_LENGTH, 100.0, WAIST_WIDTH, 40.0, HIP_WIDTH, 52.0, THIGH_WIDTH, 32.0, RISE, 30.0,
                HEM_WIDTH, 20.0)
                : Map.of(TOTAL_LENGTH, 70.0, SHOULDER_WIDTH, 50.0, CHEST_WIDTH, 58.0, SLEEVE_LENGTH, 60.0);
        return garmentRepository.save(ReferenceGarment.create(user, category, name, FitFeel.REGULAR, values));
    }
}
