package com.jiwoong.fashionfit.analysis;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.jiwoong.fashionfit.analysis.GarmentAnalysisResponse.MallLink;
import com.jiwoong.fashionfit.analysis.GarmentAnalysisResponse.SearchLinks;

// LLM 이 만든 검색어로 쇼핑몰별 검색 결과 링크를 만든다
@Component
public class ShoppingLinkBuilder {

    static final int MAX_QUERIES = 3;

    public List<SearchLinks> build(List<String> queries) {
        if (queries == null) {
            return List.of();
        }
        return queries.stream()
                .filter(q -> q != null && !q.isBlank())
                .map(String::trim)
                .distinct()
                .limit(MAX_QUERIES)
                .map(q -> new SearchLinks(q, linksFor(q)))
                .toList();
    }

    private List<MallLink> linksFor(String query) {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        return Arrays.stream(ShoppingMall.values())
                .map(mall -> new MallLink(mall, mall.getLabel(), mall.getSearchUrlTemplate().formatted(encoded)))
                .toList();
    }
}
