CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    login_id VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(30) NOT NULL,
    neighborhood CHAR(10) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_users_login_id (login_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS food_posts (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    author_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    food_name VARCHAR(100) NOT NULL,
    quantity VARCHAR(50) NOT NULL,
    expiry_date DATE NOT NULL,
    description TEXT NOT NULL,
    neighborhood CHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_food_posts_author FOREIGN KEY (author_id) REFERENCES users(id),
    CONSTRAINT ck_food_posts_status CHECK (status IN ('AVAILABLE', 'RESERVED', 'COMPLETED')),
    KEY idx_food_posts_neighborhood_created (neighborhood, created_at, id),
    KEY idx_food_posts_author (author_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RESERVED',
    active_post_id BIGINT GENERATED ALWAYS AS (
        CASE WHEN status IN ('RESERVED', 'COMPLETED') THEN post_id ELSE NULL END
    ) PERSISTENT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservations_post FOREIGN KEY (post_id) REFERENCES food_posts(id),
    CONSTRAINT fk_reservations_applicant FOREIGN KEY (applicant_id) REFERENCES users(id),
    CONSTRAINT ck_reservations_status CHECK (status IN ('RESERVED', 'CANCELLED', 'COMPLETED')),
    KEY idx_reservations_post (post_id),
    KEY idx_reservations_applicant (applicant_id),
    UNIQUE KEY uq_reservations_active_post (active_post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS chat_rooms (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_rooms_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    UNIQUE KEY uq_chat_rooms_reservation (reservation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    chat_room_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_messages_room FOREIGN KEY (chat_room_id) REFERENCES chat_rooms(id),
    CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id),
    KEY idx_chat_messages_room_id (chat_room_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
