CREATE TABLE IF NOT EXISTS member
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    login_id      VARCHAR(255) NOT NULL,
    password_hash VARCHAR(512) NOT NULL,
    name          VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (login_id)
);

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
    price   BIGINT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS reservation_slot
(
    id       BIGINT      NOT NULL AUTO_INCREMENT,
    date_id  BIGINT      NOT NULL,
    time_id  BIGINT      NOT NULL,
    theme_id BIGINT      NOT NULL,
    status   VARCHAR(20) NOT NULL,
    price    BIGINT      NOT NULL,
    version  BIGINT      NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (date_id, time_id, theme_id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (date_id) REFERENCES reservation_date (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

CREATE TABLE IF NOT EXISTS reservation
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    member_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    active_slot BOOLEAN,
    created_at TIMESTAMP NOT NULL,
    canceled_at TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE (slot_id, active_slot),
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (slot_id) REFERENCES reservation_slot (id)
);

CREATE TABLE IF NOT EXISTS waiting_reservation
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    member_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    canceled_at TIMESTAMP,
    status VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (member_id, slot_id, status),
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (slot_id) REFERENCES reservation_slot (id)
);
