-- 상품 링크로 "전에 넣은 실측표"를 찾는다 (WHERE user_id = ? AND product_url = ? ORDER BY created_at DESC LIMIT 1)
-- created_at 까지 넣어서 정렬도 인덱스로 끝낸다.
CREATE INDEX idx_size_chart_user_url ON size_chart (user_id, product_url, created_at);
