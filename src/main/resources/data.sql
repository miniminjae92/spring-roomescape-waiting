-- Users
INSERT INTO member (login_id, password_hash, name, role, created_at)
VALUES
    ('manager', '120000:cm9vbWVzY2FwZS1hZG1pbg==:++pQxwQVh3issmXrKcANjsspdq3kx9eDhGfs5tzEziQ=', '관리자', 'MANAGER', CURRENT_TIMESTAMP),
    ('user', '120000:cm9vbWVzY2FwZS1hZG1pbg==:++pQxwQVh3issmXrKcANjsspdq3kx9eDhGfs5tzEziQ=', '고래', 'USER', CURRENT_TIMESTAMP),
    ('user2', '120000:cm9vbWVzY2FwZS1hZG1pbg==:++pQxwQVh3issmXrKcANjsspdq3kx9eDhGfs5tzEziQ=', '포비', 'USER', CURRENT_TIMESTAMP),
    ('user3', '120000:cm9vbWVzY2FwZS1hZG1pbg==:++pQxwQVh3issmXrKcANjsspdq3kx9eDhGfs5tzEziQ=', '이산', 'USER', CURRENT_TIMESTAMP),
    ('user4', '120000:cm9vbWVzY2FwZS1hZG1pbg==:++pQxwQVh3issmXrKcANjsspdq3kx9eDhGfs5tzEziQ=', '보예', 'USER', CURRENT_TIMESTAMP),
    ('user5', '120000:cm9vbWVzY2FwZS1hZG1pbg==:++pQxwQVh3issmXrKcANjsspdq3kx9eDhGfs5tzEziQ=', '나무', 'USER', CURRENT_TIMESTAMP),
    ('user6', '120000:cm9vbWVzY2FwZS1hZG1pbg==:++pQxwQVh3issmXrKcANjsspdq3kx9eDhGfs5tzEziQ=', '크루', 'USER', CURRENT_TIMESTAMP);

-- Times
INSERT INTO reservation_time (start_at)
VALUES ('10:00'), ('11:30'), ('13:00'), ('14:30'), ('16:00'), ('17:30'), ('19:00'), ('20:30'), ('22:00');

-- Dates (past 7 days + future 14 days)
INSERT INTO reservation_date (play_day)
SELECT DATEADD('DAY', CAST(X - 7 AS INT), CURRENT_DATE) FROM SYSTEM_RANGE(0, 21);

-- Themes
INSERT INTO theme (name, content, url, price)
VALUES
    ('폐병원', '버려진 병원에서 들려오는 기괴한 소리들. 무사히 탈출할 수 있을까?', '/themes/abandoned-hospital.webp', 32000),
    ('고대유적', '수천 년간 감춰져 있던 고대 유적의 비밀을 파헤쳐라.', '/themes/ancient-ruins.webp', 35000),
    ('저주받은 인형방', '인형들이 살아 움직인다는 괴담이 얽힌 방.', '/themes/cursed-doll-room.webp', 30000),
    ('해적선', '저주받은 해적선에 갇혀버렸다. 보물을 찾고 탈출하자.', '/themes/cursed-pirate-ship.webp', 32000),
    ('탐정사무소', '미해결 살인 사건의 실마리를 찾기 위한 탐정의 추리.', '/themes/detective-office.webp', 30000),
    ('흉가', '마을에서 아무도 접근하지 않는 흉가에 숨겨진 진실.', '/themes/haunted-mansion.webp', 32000),
    ('잃어버린 도시', '지도에도 없는 환상의 도시를 탐험하는 모험.', '/themes/lost-city.webp', 35000),
    ('우주미아', '고장 난 우주선 안에서 생존을 위한 산소를 확보하라.', '/themes/lost-in-space.webp', 35000),
    ('기억의 연구소', '잃어버린 기억을 되찾기 위한 감성 탈출 테마.', '/themes/memory-lab.webp', 28000),
    ('프리즌 브레이크', '억울한 누명을 쓰고 갇힌 감옥에서 탈출하라.', '/themes/prison-break.webp', 32000),
    ('비밀 연구소', '불법 생체 실험이 자행되는 연구소에서 탈출.', '/themes/secret-laboratory.webp', 35000),
    ('스파이 작전', '적진에 침투해 일급 기밀문서를 탈취하라.', '/themes/spy-operation.webp', 30000),
    ('지하 던전', '괴물들이 우글거리는 어두운 지하 던전 탐험.', '/themes/underground-dungeon.webp', 32000),
    ('뱀파이어 성', '흡혈귀가 잠든 성에서 벌어지는 기괴한 이야기.', '/themes/vampire-castle.webp', 35000),
    ('마법사의 방', '견습 마법사가 되어 스승의 방에서 탈출하는 판타지.', '/themes/wizard-room.webp', 28000);

-- Slots (Create slots for all dates, all times, all themes)
INSERT INTO reservation_slot (date_id, time_id, theme_id, status, price, version)
SELECT d.id, t.id, th.id, 'OPEN', th.price, 0
FROM reservation_date d
CROSS JOIN reservation_time t
CROSS JOIN theme th;

-- Dummy Reservations for the past (To populate popular themes)
-- 폐병원 (Theme 1) -> 10 reservations
-- 우주미아 (Theme 8) -> 8 reservations
-- 프리즌 브레이크 (Theme 10) -> 6 reservations
-- 흉가 (Theme 6) -> 4 reservations
-- 마법사의 방 (Theme 15) -> 3 reservations
INSERT INTO reservation (name, member_id, slot_id, status, active_slot, created_at)
SELECT '이산', 4, s.id, 'CONFIRMED', TRUE, CURRENT_TIMESTAMP
FROM reservation_slot s
JOIN reservation_date d ON s.date_id = d.id
WHERE s.theme_id = 1 AND d.play_day BETWEEN CURRENT_DATE - 7 AND CURRENT_DATE - 1
LIMIT 10;

INSERT INTO reservation (name, member_id, slot_id, status, active_slot, created_at)
SELECT '포비', 3, s.id, 'CONFIRMED', TRUE, CURRENT_TIMESTAMP
FROM reservation_slot s
JOIN reservation_date d ON s.date_id = d.id
WHERE s.theme_id = 8 AND d.play_day BETWEEN CURRENT_DATE - 7 AND CURRENT_DATE - 1
LIMIT 8;

INSERT INTO reservation (name, member_id, slot_id, status, active_slot, created_at)
SELECT '보예', 5, s.id, 'CONFIRMED', TRUE, CURRENT_TIMESTAMP
FROM reservation_slot s
JOIN reservation_date d ON s.date_id = d.id
WHERE s.theme_id = 10 AND d.play_day BETWEEN CURRENT_DATE - 7 AND CURRENT_DATE - 1
LIMIT 6;

INSERT INTO reservation (name, member_id, slot_id, status, active_slot, created_at)
SELECT '나무', 6, s.id, 'CONFIRMED', TRUE, CURRENT_TIMESTAMP
FROM reservation_slot s
JOIN reservation_date d ON s.date_id = d.id
WHERE s.theme_id = 6 AND d.play_day BETWEEN CURRENT_DATE - 7 AND CURRENT_DATE - 1
LIMIT 4;

INSERT INTO reservation (name, member_id, slot_id, status, active_slot, created_at)
SELECT '크루', 7, s.id, 'CONFIRMED', TRUE, CURRENT_TIMESTAMP
FROM reservation_slot s
JOIN reservation_date d ON s.date_id = d.id
WHERE s.theme_id = 15 AND d.play_day BETWEEN CURRENT_DATE - 7 AND CURRENT_DATE - 1
LIMIT 3;

-- Some future reservations for user1(고래)
INSERT INTO reservation (name, member_id, slot_id, status, active_slot, created_at)
SELECT '고래', 2, s.id, 'CONFIRMED', TRUE, CURRENT_TIMESTAMP
FROM reservation_slot s
JOIN reservation_date d ON s.date_id = d.id
WHERE s.theme_id = 1 AND d.play_day = CURRENT_DATE + 1 AND s.time_id = 2
LIMIT 1;

INSERT INTO reservation (name, member_id, slot_id, status, active_slot, created_at)
SELECT '고래', 2, s.id, 'CONFIRMED', TRUE, CURRENT_TIMESTAMP
FROM reservation_slot s
JOIN reservation_date d ON s.date_id = d.id
WHERE s.theme_id = 8 AND d.play_day = CURRENT_DATE + 2 AND s.time_id = 5
LIMIT 1;

-- Active reservation required before waitlist registration
INSERT INTO reservation (name, member_id, slot_id, status, active_slot, created_at)
SELECT '포비', 3, s.id, 'CONFIRMED', TRUE, CURRENT_TIMESTAMP
FROM reservation_slot s
JOIN reservation_date d ON s.date_id = d.id
WHERE s.theme_id = 10 AND d.play_day = CURRENT_DATE + 3 AND s.time_id = 4
LIMIT 1;

-- Waitlist for 고래
INSERT INTO waiting_reservation (name, member_id, slot_id, created_at, status)
SELECT '고래', 2, s.id, CURRENT_TIMESTAMP, 'WAITING'
FROM reservation_slot s
JOIN reservation_date d ON s.date_id = d.id
WHERE s.theme_id = 10 AND d.play_day = CURRENT_DATE + 3 AND s.time_id = 4
LIMIT 1;

-- Waitlist for others
INSERT INTO waiting_reservation (name, member_id, slot_id, created_at, status)
SELECT '이산', 4, s.id, CURRENT_TIMESTAMP - INTERVAL '1' HOUR, 'WAITING'
FROM reservation_slot s
JOIN reservation_date d ON s.date_id = d.id
WHERE s.theme_id = 10 AND d.play_day = CURRENT_DATE + 3 AND s.time_id = 4
LIMIT 1;
