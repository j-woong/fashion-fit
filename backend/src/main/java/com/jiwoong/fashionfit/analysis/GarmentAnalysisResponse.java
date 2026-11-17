package com.jiwoong.fashionfit.analysis;

import java.util.List;

import com.jiwoong.fashionfit.garment.GarmentCategory;

// 프론트의 src/api/analysis.ts GarmentAnalysisResult 와 짝
public record GarmentAnalysisResponse(
        GarmentCategory category,
        String categoryLabel,
        String itemName,
        String summary,
        String color,
        String fit,
        String materialGuess,
        String textureGuess,
        List<String> materialKeywords,
        List<String> details,
        List<String> qualityChecks,
        List<SearchLinks> searches
) {

    public record SearchLinks(String query, List<MallLink> links) {
    }

    public record MallLink(ShoppingMall mall, String label, String url) {
    }

    static GarmentAnalysisResponse of(GarmentAnalysis a, List<SearchLinks> searches) {
        return new GarmentAnalysisResponse(
                a.category(),
                a.category() == null ? null : a.category().getLabel(),
                a.itemName(),
                a.summary(),
                a.color(),
                a.fit(),
                a.materialGuess(),
                a.textureGuess(),
                a.materialKeywords() == null ? List.of() : a.materialKeywords(),
                a.details() == null ? List.of() : a.details(),
                a.qualityChecks() == null ? List.of() : a.qualityChecks(),
                searches);
    }
}
