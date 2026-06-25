INSERT INTO member (login_id, password_hash, name, role, created_at)
VALUES ('manager', '120000:cm9vbWVzY2FwZS1hZG1pbg==:Rsjyx2r1Hk61aHoeG2LRlF2Sjf50zb4ph6tZqqn6mCo=', '관리자', 'MANAGER', CURRENT_TIMESTAMP),
       ('user', '120000:cm9vbWVzY2FwZS1hZG1pbg==:Rsjyx2r1Hk61aHoeG2LRlF2Sjf50zb4ph6tZqqn6mCo=', '고래', 'USER', CURRENT_TIMESTAMP);

INSERT INTO reservation_time (start_at)
VALUES ('10:00'),
       ('11:00'),
       ('13:00'),
       ('15:00'),
       ('16:00');

INSERT INTO reservation_date (play_day)
VALUES (CURRENT_DATE - 3),
       (CURRENT_DATE - 2),
       (CURRENT_DATE - 1),
       (CURRENT_DATE),
       (CURRENT_DATE + 1),
       (CURRENT_DATE + 2),
       (CURRENT_DATE + 3),
       (CURRENT_DATE + 4),
       (CURRENT_DATE + 5),
       (CURRENT_DATE + 6),
       (CURRENT_DATE + 7);

INSERT INTO theme (name, content, url, price)
VALUES ('공포', '오금이 저리는 공포입니다.', '/themes/scary', 30000),
       ('스릴러', '액션이 가미된 스릴러입니다.', '/themes/thriller', 32000),
       ('청춘물', '학교 배경인 테마 입니다.', '/themes/youth', 28000),
       ('미스터리', '단서를 따라 진실을 밝히는 추리 테마입니다.', '/themes/mystery', 30000),
       ('판타지', '마법과 전설이 살아있는 판타지 테마입니다.', '/themes/fantasy', 35000),
       ('우주', '우주정거장을 배경으로 한 SF 테마입니다.', '/themes/space', 35000),
       ('잠입', '금고를 털기 위한 잠입 작전 테마입니다.', '/themes/infiltration', 32000),
       ('재난', '제한 시간 안에 탈출해야 하는 재난 테마입니다.', '/themes/disaster', 30000),
       ('사극', '왕실의 비밀을 쫓는 사극 테마입니다.', '/themes/history', 30000),
       ('모험', '유적을 탐험하는 어드벤처 테마입니다.', '/themes/adventure', 32000),
       ('코미디', '유쾌한 소동이 가득한 코미디 테마입니다.', '/themes/comedy', 28000),
       ('느와르', '어두운 도시를 배경으로 한 느와르 테마입니다.', '/themes/noir', 30000);

INSERT INTO reservation_slot (date_id, time_id, theme_id, status, price, version)
SELECT d.id, t.id, th.id, 'OPEN', th.price, 0
FROM reservation_date d
CROSS JOIN reservation_time t
CROSS JOIN theme th
WHERE d.play_day >= CURRENT_DATE;

INSERT INTO reservation (name, member_id, slot_id, status, active_slot, created_at)
SELECT '고래', 2, id, 'CONFIRMED', TRUE, CURRENT_TIMESTAMP
FROM reservation_slot
WHERE (date_id = 5 AND time_id = 2 AND theme_id = 1)
   OR (date_id = 6 AND time_id = 2 AND theme_id = 11)
   OR (date_id = 7 AND time_id = 3 AND theme_id = 6);

-- 고래 대기 데이터
-- 슬롯1 (date+1, time2, theme1): 이산이 먼저 대기 → 고래 2순위
-- 슬롯2 (date+2, time2, theme11): 고래만 대기 → 고래 1순위
-- 슬롯3 (date+3, time3, theme6): 보예·나무가 먼저 대기 → 고래 3순위
INSERT INTO waiting_reservation (name, member_id, slot_id, created_at, status)
SELECT '고래', 2, id, CURRENT_TIMESTAMP - INTERVAL '2' HOUR, 'WAITING'
FROM reservation_slot
WHERE (date_id = 5 AND time_id = 2 AND theme_id = 1)
   OR (date_id = 6 AND time_id = 2 AND theme_id = 11)
   OR (date_id = 7 AND time_id = 3 AND theme_id = 6);
