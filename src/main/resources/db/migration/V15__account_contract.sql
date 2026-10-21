-- Ortak sözleşme (docs/API_CONTRACT.md §4.13–§4.15): kalıcı kullanıcı/gizlilik ayarları ve yardım merkezi.
CREATE TABLE IF NOT EXISTS user_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    language VARCHAR(10) NOT NULL DEFAULT 'tr',
    timezone VARCHAR(50) NOT NULL DEFAULT 'Europe/Istanbul',
    currency VARCHAR(10) NOT NULL DEFAULT 'TRY',
    email_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    sms_notifications BOOLEAN NOT NULL DEFAULT FALSE,
    push_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    marketing_emails BOOLEAN NOT NULL DEFAULT FALSE,
    order_updates BOOLEAN NOT NULL DEFAULT TRUE,
    price_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    stock_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    theme VARCHAR(20) NOT NULL DEFAULT 'light',
    items_per_page INTEGER NOT NULL DEFAULT 20,
    auto_save_cart BOOLEAN NOT NULL DEFAULT TRUE,
    show_product_recommendations BOOLEAN NOT NULL DEFAULT TRUE,
    enable_location_services BOOLEAN NOT NULL DEFAULT FALSE,
    login_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    two_factor_required BOOLEAN NOT NULL DEFAULT FALSE,
    session_timeout INTEGER NOT NULL DEFAULT 30,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS privacy_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    profile_visibility BOOLEAN NOT NULL DEFAULT TRUE,
    show_email BOOLEAN NOT NULL DEFAULT FALSE,
    show_phone BOOLEAN NOT NULL DEFAULT FALSE,
    allow_data_collection BOOLEAN NOT NULL DEFAULT TRUE,
    allow_analytics BOOLEAN NOT NULL DEFAULT TRUE,
    allow_cookies BOOLEAN NOT NULL DEFAULT TRUE,
    allow_marketing BOOLEAN NOT NULL DEFAULT FALSE,
    data_sharing BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS help_articles (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    tags TEXT NOT NULL DEFAULT '',
    view_count INTEGER NOT NULL DEFAULT 0,
    is_published BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS faqs (
    id BIGSERIAL PRIMARY KEY,
    question VARCHAR(500) NOT NULL,
    answer TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    view_count INTEGER NOT NULL DEFAULT 0,
    is_published BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS support_tickets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    subject VARCHAR(200) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    category VARCHAR(50) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'Medium',
    status VARCHAR(20) NOT NULL DEFAULT 'Open',
    assigned_to VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS support_messages (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES support_tickets (id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    message VARCHAR(1000) NOT NULL,
    is_from_support BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_support_tickets_user ON support_tickets (user_id);
CREATE INDEX IF NOT EXISTS idx_support_messages_ticket ON support_messages (ticket_id);
CREATE INDEX IF NOT EXISTS idx_faqs_category ON faqs (category);
CREATE INDEX IF NOT EXISTS idx_help_articles_category ON help_articles (category);
