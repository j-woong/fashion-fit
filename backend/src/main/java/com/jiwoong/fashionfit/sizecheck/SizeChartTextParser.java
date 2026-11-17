package com.jiwoong.fashionfit.sizecheck;

import static com.jiwoong.fashionfit.garment.MeasurementPart.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.jiwoong.fashionfit.garment.MeasurementPart;

// 상품 설명에서 복사한 "글자" 실측을 읽는다. LLM 을 부르지 않아서 바로 끝나고, 같은 글자에는 늘 같은 결과가 나온다.
// (이미지 → LLM 은 10초쯤 걸리고 가끔 틀린다. 글자로 적혀 있는 쇼핑몰은 이쪽이 빠르고 정확하다)
//
// 읽을 수 있는 모양 3가지
//   ① 한 줄 나열   "M (추천 95~100) - 어깨 52 / 가슴 58 / 소매 62 / 총장 68"
//   ② 표           "사이즈 총장 어깨 가슴 소매" 다음 줄부터 "M 68 52 58 62"
//   ③ 뒤집힌 표    "S M L" 다음 줄부터 "총장 68 70 72"
// 규칙: "둘레"는 반으로 나눠 단면으로 바꾸고, 인치는 2.54 를 곱한다.
// 여기서는 읽기만 한다. 카테고리에 맞는 부위만 남기고 값 범위를 거르는 일은 SizeChartExtractionService 가 한다.
public final class SizeChartTextParser {

    // 부위를 가리키는 낱말. part 가 null 이면 "우리가 안 쓰는 부위"라서 읽고 버린다
    // (예: "소매통 20"을 버리지 않으면 "소매"로 잘못 읽는다).
    private record Alias(String word, MeasurementPart part) {
    }

    private static final List<Alias> ALIASES = List.of(
            new Alias("총장", TOTAL_LENGTH), new Alias("총기장", TOTAL_LENGTH), new Alias("총길이", TOTAL_LENGTH),
            new Alias("전체길이", TOTAL_LENGTH), new Alias("기장", TOTAL_LENGTH),
            new Alias("total length", TOTAL_LENGTH), new Alias("length", TOTAL_LENGTH),
            new Alias("어깨", SHOULDER_WIDTH), new Alias("shoulder", SHOULDER_WIDTH),
            new Alias("가슴", CHEST_WIDTH), new Alias("chest", CHEST_WIDTH), new Alias("bust", CHEST_WIDTH),
            new Alias("소매", SLEEVE_LENGTH), new Alias("팔길이", SLEEVE_LENGTH), new Alias("sleeve", SLEEVE_LENGTH),
            new Alias("허리", WAIST_WIDTH), new Alias("waist", WAIST_WIDTH),
            new Alias("엉덩이", HIP_WIDTH), new Alias("힙", HIP_WIDTH), new Alias("hip", HIP_WIDTH),
            new Alias("허벅지", THIGH_WIDTH), new Alias("thigh", THIGH_WIDTH),
            new Alias("밑위", RISE), new Alias("앞밑위", RISE), new Alias("rise", RISE),
            new Alias("밑단", HEM_WIDTH), new Alias("hem", HEM_WIDTH),
            new Alias("소매통", null), new Alias("소매단", null), new Alias("소매밑단", null), new Alias("암홀", null),
            new Alias("화장", null), new Alias("뒷밑위", null), new Alias("목너비", null));

    // 긴 낱말부터 맞춰 봐야 "소매통"이 "소매"로 읽히지 않는다
    private static final String ALIAS_GROUP = ALIASES.stream()
            .map(Alias::word)
            .sorted(Comparator.comparingInt(String::length).reversed())
            .map(Pattern::quote)
            .collect(Collectors.joining("|"));
    private static final String SUFFIX_GROUP = "전체둘레|전체|둘레|단면|너비|넓이|길이|기장|폭|length|width";
    private static final String NUMBER = "\\d+(?:\\.\\d+)?";

    // "가슴 58", "가슴단면: 58", "허리둘레 82" → (낱말)(꼬리말)(숫자)
    private static final Pattern KEY_VALUE = Pattern.compile(
            "(?i)(" + ALIAS_GROUP + ")\\s*(" + SUFFIX_GROUP + ")?\\s*[:=]?\\s*(" + NUMBER + ")");
    // 표의 머리칸 하나: "가슴단면", "총장"
    private static final Pattern HEADER_CELL = Pattern.compile("(?i)(" + ALIAS_GROUP + ")(" + SUFFIX_GROUP + ")?");
    // 뒤집힌 표의 한 줄: "총장 68 70 72"
    private static final Pattern TRANSPOSED_ROW = Pattern.compile(
            "(?i)^(" + ALIAS_GROUP + ")\\s*(" + SUFFIX_GROUP + ")?\\s*[:=]?\\s*((?:" + NUMBER + "[\\s/,|]+)+" + NUMBER + ")$");
    private static final Pattern SIZE_LABEL = Pattern.compile(
            "(?i)X{0,3}S|M|X{0,3}L|[2-5]XL|F|FREE|OS|ONE ?SIZE|프리|\\d{2,3}");
    private static final Pattern SEPARATORS = Pattern.compile("[-:/|,·•*=~]");
    private static final String DEFAULT_LABEL = "FREE";

    private SizeChartTextParser() {
    }

    // 결과: { "M": {SHOULDER_WIDTH: 52, ...}, "L": {...} } (글에 나온 순서 유지). 못 읽으면 빈 Map.
    public static Map<String, Map<MeasurementPart, Double>> parse(String text) {
        if (text == null || text.isBlank()) {
            return Map.of();
        }
        // 단위는 괄호를 지우기 전에 본다 ("사이즈(inch)")
        String lower = text.toLowerCase();
        double unit = (lower.contains("inch") || lower.contains("인치")) && !lower.contains("cm") ? 2.54 : 1;

        List<String> lines = Arrays.stream(text.split("\\R"))
                .map(SizeChartTextParser::clean)
                .filter(line -> !line.isEmpty())
                .toList();

        Map<String, Map<MeasurementPart, Double>> rows = parseTable(lines, unit);
        if (rows.isEmpty()) {
            rows = parseTransposed(lines, unit);
        }
        if (rows.isEmpty()) {
            rows = parseInline(lines, unit);
        }
        return rows;
    }

    // "소매(62)", "총장 (68cm)" 처럼 부위 바로 뒤 괄호에 값이 들어 있는 표기
    private static final Pattern VALUE_IN_PARENS = Pattern.compile(
            "(?i)(" + ALIAS_GROUP + ")\\s*(" + SUFFIX_GROUP + ")?\\s*\\(\\s*(" + NUMBER + ")\\s*(?:cm)?\\s*\\)");

    // 괄호 안(추천 사이즈 95~100 같은 설명)과 cm 표시는 숫자를 헷갈리게 해서 지운다.
    // 단, 부위 바로 뒤 괄호의 숫자는 실측값이라서 지우기 전에 꺼내 둔다.
    private static String clean(String line) {
        return VALUE_IN_PARENS.matcher(line).replaceAll("$1$2 $3 ")
                .replaceAll("\\([^()]*\\)", " ")
                .replaceAll("(?i)(?<=\\d)\\s*cm", " ")
                .replaceAll("(?i)\\bcm\\b", " ")
                .replaceAll("[ \\u00A0]+", " ")
                .strip();
    }

    // ---------- ② 표 ----------
    private static Map<String, Map<MeasurementPart, Double>> parseTable(List<String> lines, double unit) {
        Map<String, Map<MeasurementPart, Double>> rows = new LinkedHashMap<>();
        List<Column> header = null;

        for (String line : lines) {
            List<Column> candidate = headerOf(line);
            if (candidate != null) {
                header = candidate;
                continue;
            }
            if (header == null) {
                continue;
            }
            List<String> cells = split(line);
            // 머리줄에 "사이즈" 칸이 있으면 칸 수가 같고, 없으면 값 줄이 한 칸 더 많다
            int offset = cells.size() - header.size();
            boolean labelColumnInHeader = offset == 0 && !header.getFirst().known();
            boolean labelColumnMissing = offset == 1 && header.getFirst().known();
            if (!labelColumnInHeader && !labelColumnMissing) {
                continue;
            }
            String label = cells.getFirst().strip();
            if (label.isEmpty()) {
                continue;
            }
            for (int i = 1; i < cells.size(); i++) {
                Column column = header.get(i - offset);
                Double value = toNumber(cells.get(i));
                if (column.part() != null && value != null) {
                    put(rows, label, column.part(), column.convert(value, unit));
                }
            }
        }
        return rows;
    }

    // 표의 한 칸이 가리키는 부위. known 은 "아는 낱말인지"(안 쓰는 부위도 아는 낱말이다)
    private record Column(MeasurementPart part, boolean known, boolean circumference) {

        double convert(double value, double unit) {
            return (circumference ? value / 2 : value) * unit;
        }
    }

    // 숫자가 없고 부위 낱말이 2개 이상이면 머리줄로 본다
    private static List<Column> headerOf(String line) {
        if (line.matches(".*\\d.*")) {
            return null;
        }
        // "가슴 단면"처럼 띄어 쓴 꼬리말을 붙여서 한 칸으로 만든다
        String joined = line.replaceAll("(?i)\\s+(" + SUFFIX_GROUP + ")(?=\\s|$)", "$1");
        List<Column> columns = new ArrayList<>();
        int knownCount = 0;
        for (String cell : split(joined)) {
            Matcher m = HEADER_CELL.matcher(cell);
            if (m.matches()) {
                columns.add(new Column(partOf(m.group(1)), true, isCircumference(m.group(2))));
                knownCount++;
            } else {
                columns.add(new Column(null, false, false));
            }
        }
        return knownCount >= 2 ? columns : null;
    }

    // 엑셀·표에서 복사하면 탭으로 나뉜다. 탭이 있으면 빈 칸도 자리를 지켜야 해서 탭으로만 자른다
    private static List<String> split(String line) {
        return line.contains("\t") ? Arrays.asList(line.split("\t", -1)) : Arrays.asList(line.split("\\s+"));
    }

    // ---------- ③ 뒤집힌 표 ----------
    private static Map<String, Map<MeasurementPart, Double>> parseTransposed(List<String> lines, double unit) {
        Map<String, Map<MeasurementPart, Double>> rows = new LinkedHashMap<>();
        List<String> labels = null;

        for (String line : lines) {
            Matcher m = TRANSPOSED_ROW.matcher(line);
            if (!m.matches()) {
                List<String> candidate = labelsOf(line);
                if (candidate != null) {
                    labels = candidate;
                }
                continue;
            }
            MeasurementPart part = partOf(m.group(1));
            String[] values = m.group(3).split("[\\s/,|]+");
            if (part == null) {
                continue;
            }
            for (int i = 0; i < values.length; i++) {
                // 사이즈 이름 줄을 못 찾았으면 "1번", "2번"으로 채워 두고 화면에서 고치게 한다
                String label = labels != null && labels.size() == values.length ? labels.get(i) : (i + 1) + "번";
                double value = Double.parseDouble(values[i]);
                put(rows, label, part, (isCircumference(m.group(2)) ? value / 2 : value) * unit);
            }
        }
        return rows;
    }

    // "사이즈 S M L" 처럼 사이즈 이름만 있는 줄
    private static List<String> labelsOf(String line) {
        List<String> labels = Arrays.stream(SEPARATORS.matcher(line).replaceAll(" ").split("\\s+"))
                .filter(token -> SIZE_LABEL.matcher(token).matches())
                .map(String::toUpperCase)
                .toList();
        return labels.size() >= 2 ? labels : null;
    }

    // ---------- ① 한 줄 나열 ----------
    private static Map<String, Map<MeasurementPart, Double>> parseInline(List<String> lines, double unit) {
        Map<String, Map<MeasurementPart, Double>> rows = new LinkedHashMap<>();
        String pending = null;   // 이름만 있는 줄에서 본 사이즈 ("M" 다음 줄에 "어깨 52 ...")
        String current = null;

        for (String line : lines) {
            Matcher m = KEY_VALUE.matcher(line);
            int previousEnd = 0;
            boolean found = false;
            while (m.find()) {
                // 앞 값과 이번 값 사이에 사이즈 이름이 끼어 있으면 새 사이즈가 시작된 것이다
                String label = labelFrom(line.substring(previousEnd, m.start()), !found);
                if (label != null) {
                    current = label;
                } else if (!found && pending != null) {
                    current = pending;
                } else if (current == null) {
                    current = DEFAULT_LABEL;
                }
                pending = null;
                found = true;
                previousEnd = m.end();

                MeasurementPart part = partOf(m.group(1));
                if (part != null) {
                    double value = Double.parseDouble(m.group(3));
                    put(rows, current, part, (isCircumference(m.group(2)) ? value / 2 : value) * unit);
                }
            }
            if (!found) {
                // 숫자만 있는 줄("10")은 후기 수 같은 다른 정보일 수 있어서 이름으로 보지 않는다
                String label = labelFrom(line, false);
                if (label != null && !label.matches(NUMBER)) {
                    pending = label;
                }
            }
        }
        return rows;
    }

    // 값 앞에 붙은 글자에서 사이즈 이름을 찾는다. "M - " → M, "사이즈 : " → 없음
    // lenient: 줄 맨 앞이면 "1호" 같은 낯선 이름도 받아 준다 (줄 중간에서는 S·M·L·95 같은 확실한 이름만)
    private static String labelFrom(String gap, boolean lenient) {
        String cleaned = SEPARATORS.matcher(gap.replaceAll("(?i)사이즈|size", " ")).replaceAll(" ").strip();
        if (cleaned.isEmpty()) {
            return null;
        }
        String[] tokens = cleaned.split("\\s+");
        // 줄 맨 앞에서는 첫 낱말이 사이즈 이름이다 ("F : 어깨무관 70 / 가슴 60" 의 70 을 이름으로 읽지 않는다)
        if (lenient && SIZE_LABEL.matcher(tokens[0]).matches()) {
            return tokens[0].toUpperCase();
        }
        String last = tokens[tokens.length - 1];
        if (SIZE_LABEL.matcher(last).matches()) {
            return last.toUpperCase();
        }
        boolean oneShortWord = tokens.length == 1 && cleaned.length() <= 10 && !cleaned.matches(NUMBER);
        return lenient && oneShortWord ? cleaned : null;
    }

    // "가슴둘레 116", "가슴 전체 116" 은 한 바퀴 길이라서 반으로 나눠야 단면이 된다
    private static boolean isCircumference(String suffix) {
        return suffix != null && (suffix.endsWith("둘레") || suffix.equals("전체"));
    }

    private static MeasurementPart partOf(String word) {
        return ALIASES.stream()
                .filter(alias -> alias.word().equalsIgnoreCase(word))
                .findFirst()
                .map(Alias::part)
                .orElse(null);
    }

    private static Double toNumber(String cell) {
        String trimmed = cell.strip();
        return trimmed.matches(NUMBER) ? Double.valueOf(trimmed) : null;
    }

    // 같은 사이즈·부위가 두 번 나오면 먼저 나온 값을 쓴다
    private static void put(Map<String, Map<MeasurementPart, Double>> rows, String label, MeasurementPart part,
                            double value) {
        rows.computeIfAbsent(label, k -> new EnumMap<>(MeasurementPart.class)).putIfAbsent(part, value);
    }
}
