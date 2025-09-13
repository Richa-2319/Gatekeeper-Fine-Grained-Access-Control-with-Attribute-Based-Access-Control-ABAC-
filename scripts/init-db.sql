// Create: src/main/resources/db/migration/V001__Initial_schema.sql
-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role VARCHAR(20) DEFAULT 'user',
    department VARCHAR(50),
    location VARCHAR(50),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- Create policies table
CREATE TABLE IF NOT EXISTS policies (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    rego_rule TEXT NOT NULL,
    description TEXT,
    resource VARCHAR(100) DEFAULT '*',
    action VARCHAR(50) DEFAULT '*',
    active BOOLEAN DEFAULT true,
    priority INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create audit_logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(50),
    resource VARCHAR(100),
    action VARCHAR(50),
    decision VARCHAR(10),
    reason TEXT,
    client_ip VARCHAR(45),
    user_agent TEXT,
    request_context TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    evaluation_time_ms BIGINT
);

-- Create user_attributes table
CREATE TABLE IF NOT EXISTS user_attributes (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    attribute_name VARCHAR(100),
    attribute_value VARCHAR(500),
    PRIMARY KEY (user_id, attribute_name)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_policies_name ON policies(name);
CREATE INDEX IF NOT EXISTS idx_policies_resource_action ON policies(resource, action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON audit_logs(timestamp);
