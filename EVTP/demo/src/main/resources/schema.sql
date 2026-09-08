-- Bang danh sach qua
CREATE TABLE IF NOT EXISTS prizes (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    daily_quota INT DEFAULT 0,
    image_url TEXT
);

-- Bang luot quay user
CREATE TABLE IF NOT EXISTS user_spins (
    user_id VARCHAR(50) PRIMARY KEY,
    total_earned INT DEFAULT 0,
    total_used INT DEFAULT 0,
    last_spin_time TIMESTAMP
);

-- Bang lich su quay
CREATE TABLE IF NOT EXISTS spin_history (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    prize_id INT NOT NULL,
    prize_name VARCHAR(100),
    is_win BOOLEAN,
    n_index INT,
    s_index BIGINT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Data mau: Danh sach qua
INSERT INTO prizes (id, name, type, daily_quota) VALUES (1, 'May in nhiet', 'REAL', 10) ON CONFLICT (id) DO NOTHING;
INSERT INTO prizes (id, name, type, daily_quota) VALUES (2, 'PDA Zebra', 'REAL', 2) ON CONFLICT (id) DO NOTHING;
INSERT INTO prizes (id, name, type, daily_quota) VALUES (999, 'Chuc may man lan sau', 'LUCKY', 0) ON CONFLICT (id) DO NOTHING;

-- Data mau: User test
INSERT INTO user_spins (user_id, total_earned, total_used) VALUES ('buuta_01', 50, 0) ON CONFLICT (user_id) DO NOTHING;
INSERT INTO user_spins (user_id, total_earned, total_used) VALUES ('buuta_02', 30, 0) ON CONFLICT (user_id) DO NOTHING;
