-- Insert if user doesn't already exist
INSERT INTO users (username, email, password, enabled, created_at, updated_at)
SELECT 'johndoe',
       'johndoe@example.com',
       '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
       true,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'johndoe'
);
