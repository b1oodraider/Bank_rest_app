-- Bank REST API Database Schema
-- PostgreSQL

-- Create database (run this separately if needed)
-- CREATE DATABASE bankdb;

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User roles table (many-to-many relationship)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Cards table
CREATE TABLE cards (
    id BIGSERIAL PRIMARY KEY,
    encrypted_number VARCHAR(255) NOT NULL UNIQUE,
    masked_number VARCHAR(19) NOT NULL,
    owner VARCHAR(255) NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Transfers table
CREATE TABLE transfers (
    id BIGSERIAL PRIMARY KEY,
    from_card_id BIGINT NOT NULL,
    to_card_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (from_card_id) REFERENCES cards(id) ON DELETE CASCADE,
    FOREIGN KEY (to_card_id) REFERENCES cards(id) ON DELETE CASCADE
);

-- Indexes for better performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_cards_user_id ON cards(user_id);
CREATE INDEX idx_cards_status ON cards(status);
CREATE INDEX idx_cards_expiry_date ON cards(expiry_date);
CREATE INDEX idx_transfers_from_card ON transfers(from_card_id);
CREATE INDEX idx_transfers_to_card ON transfers(to_card_id);
CREATE INDEX idx_transfers_timestamp ON transfers(timestamp);

-- Constraints
ALTER TABLE cards ADD CONSTRAINT chk_card_status 
    CHECK (status IN ('ACTIVE', 'BLOCKED', 'EXPIRED'));

ALTER TABLE cards ADD CONSTRAINT chk_card_balance 
    CHECK (balance >= 0);

ALTER TABLE transfers ADD CONSTRAINT chk_transfer_amount 
    CHECK (amount > 0);

ALTER TABLE transfers ADD CONSTRAINT chk_transfer_different_cards 
    CHECK (from_card_id != to_card_id);

-- Triggers for updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_cards_updated_at 
    BEFORE UPDATE ON cards 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert initial admin user (password: admin123)
INSERT INTO users (username, password) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa');

-- Insert admin role
INSERT INTO user_roles (user_id, role) VALUES 
(1, 'ROLE_ADMIN');

-- Insert sample user (password: user123)
INSERT INTO users (username, password) VALUES 
('user', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a');

-- Insert user role
INSERT INTO user_roles (user_id, role) VALUES 
(2, 'ROLE_USER'); 