CREATE TABLE IF NOT EXISTS reservation_time
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    start_at VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS reservation_date
(
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    play_day VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS theme
(
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255) NOT NULL,
    content VARCHAR(255) NOT NULL,
    url     VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS reservation
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    date_id  BIGINT NOT NULL,
    time_id  BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,
    status   VARCHAR(20) NOT NULL DEFAULT 'RESERVED',
    active_slot BOOLEAN GENERATED ALWAYS AS (
        CASE WHEN status = 'RESERVED' THEN TRUE ELSE NULL END
    ),
    PRIMARY KEY (id),
    CONSTRAINT unique_active_reservation_slot UNIQUE (date_id, time_id, theme_id, active_slot),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (date_id) REFERENCES reservation_date (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE TABLE IF NOT EXISTS waiting_reservation
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    date_id  BIGINT NOT NULL,
    time_id  BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    promoted_reservation_id BIGINT,
    active_waiting BOOLEAN GENERATED ALWAYS AS (
        CASE WHEN status = 'WAITING' THEN TRUE ELSE NULL END
    ),
    PRIMARY KEY (id),
    CONSTRAINT unique_active_waiting_slot UNIQUE (name, date_id, time_id, theme_id, active_waiting),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (date_id) REFERENCES reservation_date (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (promoted_reservation_id) REFERENCES reservation (id) ON DELETE SET NULL
);
