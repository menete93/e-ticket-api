CREATE EXTENSION postgis;

SELECT extname, extnamespace::regnamespace
FROM pg_extension
WHERE extname = 'postgis';


DROP EXTENSION postgis CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;
ALTER DATABASE e_ticket SET search_path = public, e_ticket;
CREATE EXTENSION postgis SCHEMA public;




CREATE TABLE event_categories (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
name VARCHAR(100) NOT NULL UNIQUE,
description TEXT,
-- Auditoria
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by VARCHAR(50) NOT NULL,
updated_at TIMESTAMP DEFAULT NULL,
updated_by VARCHAR(50) DEFAULT NULL,
-- Ciclo de vida
life_cycle_state INT NOT NULL DEFAULT 0, -- 0 = ACTIVE, 1 = INACTIVE, 2 = DELETED, 3 = BLOCKED, 4 = BANNED
);
--------------------------------------------------------VENUES-----------------------------------------------------------
CREATE TABLE venues (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
owner_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
name VARCHAR(150) NOT NULL,
description TEXT,
capacity INT NOT NULL,
latitude DOUBLE PRECISION,
longitude DOUBLE PRECISION,
location GEOGRAPHY(POINT, 4326),
address VARCHAR(255),
open_time TIME,
close_time TIME,
image_url VARCHAR(255),
    -- Auditoria
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by VARCHAR(50) NOT NULL,
updated_at TIMESTAMP DEFAULT NULL,
updated_by VARCHAR(50) DEFAULT NULL,
-- Ciclo de vida
life_cycle_state INT NOT NULL DEFAULT 0, -- 0 = ACTIVE, 1 = INACTIVE, 2 = DELETED, 3 = BLOCKED, 4 = BANNED
);
CREATE INDEX idx_venues_location ON venues USING GIST(location);
--------------------------------------------------------EVENTS-----------------------------------------------------------
CREATE TABLE events (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
organizer_id BIGINT REFERENCES users(id),
venue_id BIGINT REFERENCES venues(id),
name VARCHAR(200) NOT NULL,
description TEXT,
category_id BIGINT REFERENCES event_categories(id),
start_time TIMESTAMP NOT NULL,
end_time TIMESTAMP,
capacity INT,
seat_type VARCHAR(20) DEFAULT 'FREE',
latitude DOUBLE PRECISION,
longitude DOUBLE PRECISION,
location GEOGRAPHY(POINT, 4326),
status VARCHAR(20) DEFAULT 'DRAFT',
image_url VARCHAR(255),
    -- Auditoria
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by VARCHAR(50) NOT NULL,
updated_at TIMESTAMP DEFAULT NULL,
updated_by VARCHAR(50) DEFAULT NULL,
-- Ciclo de vida
life_cycle_state INT NOT NULL DEFAULT 0, -- 0 = ACTIVE, 1 = INACTIVE, 2 = DELETED, 3 = BLOCKED, 4 = BANNED
);
CREATE INDEX idx_events_location ON events USING GIST(location);
CREATE INDEX idx_events_category ON events(category_id);
--------------------------------------------------------TICKETS'CATEGORIES-----------------------------------------------------------
CREATE TABLE ticket_categories (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
event_id BIGINT REFERENCES events(id) ON DELETE CASCADE,
name VARCHAR(50) NOT NULL,
description TEXT,
base_price DECIMAL(10,2) NOT NULL,
total_quantity INT,
allocated_quantity INT DEFAULT 0,
invite_condition JSONB,
    -- Auditoria
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by VARCHAR(50) NOT NULL,
updated_at TIMESTAMP DEFAULT NULL,
updated_by VARCHAR(50) DEFAULT NULL,
-- Ciclo de vida
life_cycle_state INT NOT NULL DEFAULT 0, -- 0 = ACTIVE, 1 = INACTIVE, 2 = DELETED, 3 = BLOCKED, 4 = BANNED
);
--------------------------------------------------------SALE-PHASE-----------------------------------------------------------
CREATE TABLE sale_phases (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
event_id BIGINT REFERENCES events(id) ON DELETE CASCADE,
name VARCHAR(50),
start_time TIMESTAMP NOT NULL,
end_time TIMESTAMP NOT NULL,
price_multiplier DECIMAL(5,2) DEFAULT 1.0,
    -- Auditoria
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by VARCHAR(50) NOT NULL,
updated_at TIMESTAMP DEFAULT NULL,
updated_by VARCHAR(50) DEFAULT NULL,
-- Ciclo de vida
life_cycle_state INT NOT NULL DEFAULT 0, -- 0 = ACTIVE, 1 = INACTIVE, 2 = DELETED, 3 = BLOCKED, 4 = BANNED
);
--------------------------------------------------------TICKET------------------------------------------------------------
CREATE TABLE tickets (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
event_id BIGINT REFERENCES events(id) ON DELETE CASCADE,
user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
category_id BIGINT REFERENCES ticket_categories(id),
price DECIMAL(10,2),
status VARCHAR(20) DEFAULT 'ACTIVE',
seat_number VARCHAR(30),
qr_code VARCHAR(255),
qr_signature VARCHAR(255),
    -- Auditoria
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by VARCHAR(50) NOT NULL,
updated_at TIMESTAMP DEFAULT NULL,
updated_by VARCHAR(50) DEFAULT NULL,
-- Ciclo de vida
life_cycle_state INT NOT NULL DEFAULT 0, -- 0 = ACTIVE, 1 = INACTIVE, 2 = DELETED, 3 = BLOCKED, 4 = BANNED
);
--------------------------------------------------------RESERVATION------------------------------------------------------------
CREATE TABLE reservations (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
venue_id BIGINT REFERENCES venues(id) ON DELETE CASCADE,
user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
reservation_start TIMESTAMP NOT NULL,
reservation_end TIMESTAMP,
num_people INT,
status VARCHAR(20) DEFAULT 'PENDING',
qr_code VARCHAR(255),
payment_status VARCHAR(20) DEFAULT 'UNPAID',
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
--------------------------------------------------------PAYMENTS------------------------------------------------------------
CREATE TABLE payments (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
user_id BIGINT REFERENCES users(id),
amount DECIMAL(10,2) NOT NULL,
currency VARCHAR(10) DEFAULT 'MZN',
method VARCHAR(50),
provider_payment_id VARCHAR(150),
status VARCHAR(20) DEFAULT 'PENDING',
related_type VARCHAR(30),
related_id BIGINT,
    -- Auditoria
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by VARCHAR(50) NOT NULL,
updated_at TIMESTAMP DEFAULT NULL,
updated_by VARCHAR(50) DEFAULT NULL,
-- Ciclo de vida
life_cycle_state INT NOT NULL DEFAULT 0, -- 0 = ACTIVE, 1 = INACTIVE, 2 = DELETED, 3 = BLOCKED, 4 = BANNED
);
--------------------------------------------------------PAYMENTS------------------------------------------------------------
CREATE TABLE audit_logs (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
entity_type VARCHAR(100),
entity_id BIGINT,
action VARCHAR(50),
performed_by BIGINT REFERENCES users(id),
details JSONB,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
