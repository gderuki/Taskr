-- Insert test user with BCrypt hashed password
-- Username: testuser
-- Password: password
-- BCrypt hash generated with strength 10

-- Delete existing test user if exists
DELETE FROM users WHERE username = 'testuser';

-- Insert test user
-- The password 'password123' is hashed using BCrypt
INSERT INTO users (username, email, password, enabled, created_at, updated_at)
VALUES (
    'testuser',
    'testuser@example.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
