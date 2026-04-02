CREATE TABLE bookings
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    user_id          BIGINT                NOT NULL,
    table_id         BIGINT                NOT NULL,
    booking_time     datetime              NULL,
    number_of_guests INT                   NOT NULL,
    note             VARCHAR(255)          NULL,
    status           VARCHAR(255)          NULL,
    total_amount     DECIMAL               NULL,
    CONSTRAINT pk_bookings PRIMARY KEY (id)
);

CREATE TABLE carts
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    user_id    BIGINT                NULL,
    dish_id    BIGINT                NULL,
    quantity   INT                   NOT NULL,
    created_at datetime              NULL,
    CONSTRAINT pk_carts PRIMARY KEY (id)
);

CREATE TABLE daily_statistics
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    stat_date       date                  NULL,
    total_bookings  INT                   NULL,
    total_revenue   DECIMAL               NULL,
    total_customers INT                   NULL,
    popular_dish_id BIGINT                NULL,
    created_at      datetime              NULL,
    version         BIGINT                NULL,
    CONSTRAINT pk_daily_statistics PRIMARY KEY (id)
);

CREATE TABLE dishes
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    name           VARCHAR(255)          NULL,
    `description`  VARCHAR(255)          NULL,
    price          DECIMAL               NULL,
    available      BIT(1)                NULL,
    category       VARCHAR(255)          NULL,
    version        BIGINT                NULL,
    created_at     datetime              NULL,
    order_count    INT                   NOT NULL,
    average_rating DECIMAL(3, 2)         NULL,
    total_reviews  INT                   NOT NULL,
    updated_at     datetime              NULL,
    updated_by     VARCHAR(255)          NULL,
    CONSTRAINT pk_dishes PRIMARY KEY (id)
);

CREATE TABLE images
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    url         VARCHAR(255)          NULL,
    uploaded_at datetime              NULL,
    user_id     BIGINT                NULL,
    dish_id     BIGINT                NULL,
    is_avatar   BIT(1)                NOT NULL,
    CONSTRAINT pk_images PRIMARY KEY (id)
);

CREATE TABLE notifications
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    title      VARCHAR(255)          NULL,
    content    VARCHAR(255)          NULL,
    is_read    BIT(1)                NULL,
    created_at datetime              NULL,
    to_user_id BIGINT                NULL,
    CONSTRAINT pk_notifications PRIMARY KEY (id)
);

CREATE TABLE order_history
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    created_at   datetime              NOT NULL,
    created_by   VARCHAR(100)          NULL,
    updated_at   datetime              NULL,
    updated_by   VARCHAR(100)          NULL,
    booking_id   BIGINT                NOT NULL,
    dish_id      BIGINT                NOT NULL,
    user_id      BIGINT                NOT NULL,
    quantity     INT                   NOT NULL,
    served       BIT(1)                NOT NULL,
    note         TEXT                  NULL,
    total_amount DECIMAL(10, 2)        NOT NULL,
    CONSTRAINT pk_order_history PRIMARY KEY (id)
);

CREATE TABLE outbox_events
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    event_id       VARCHAR(255)          NOT NULL,
    event_type     VARCHAR(255)          NOT NULL,
    aggregate_type VARCHAR(255)          NOT NULL,
    aggregate_id   BIGINT                NOT NULL,
    payload        TEXT                  NOT NULL,
    status         VARCHAR(255)          NOT NULL,
    created_at     datetime              NOT NULL,
    published_at   datetime              NULL,
    retry_count    INT                   NOT NULL,
    max_retries    INT                   NOT NULL,
    error_message  TEXT                  NULL,
    next_retry_at  datetime              NULL,
    CONSTRAINT pk_outbox_events PRIMARY KEY (id)
);

CREATE TABLE payments
(
    id                    BIGINT AUTO_INCREMENT NOT NULL,
    deleted_at            datetime              NULL,
    deleted_by            VARCHAR(100)          NULL,
    created_at            datetime              NOT NULL,
    created_by            VARCHAR(100)          NULL,
    updated_at            datetime              NULL,
    updated_by            VARCHAR(100)          NULL,
    booking_id            BIGINT                NOT NULL,
    amount                DECIMAL(10, 2)        NOT NULL,
    payment_method        VARCHAR(50)           NOT NULL,
    payment_time          datetime              NULL,
    status                VARCHAR(20)           NOT NULL,
    transaction_reference VARCHAR(100)          NULL,
    customer_note         TEXT                  NULL,
    admin_note            TEXT                  NULL,
    processed_at          datetime              NULL,
    processed_by          VARCHAR(100)          NULL,
    CONSTRAINT pk_payments PRIMARY KEY (id)
);

CREATE TABLE preorders
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime              NOT NULL,
    created_by VARCHAR(100)          NULL,
    updated_at datetime              NULL,
    updated_by VARCHAR(100)          NULL,
    booking_id BIGINT                NOT NULL,
    dish_id    BIGINT                NOT NULL,
    quantity   INT                   NOT NULL,
    note       TEXT                  NULL,
    CONSTRAINT pk_preorders PRIMARY KEY (id)
);

CREATE TABLE refresh_token
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    token           VARCHAR(767)          NOT NULL,
    user_id         BIGINT                NOT NULL,
    expiry_date     datetime              NOT NULL,
    created_at      datetime              NOT NULL,
    created_by      VARCHAR(100)          NULL,
    updated_at      datetime              NULL,
    updated_by      VARCHAR(100)          NULL,
    deleted_at      datetime              NULL,
    deleted_by      VARCHAR(100)          NULL,
    revoked         BIT(1)                NOT NULL,
    revoked_at      datetime              NULL,
    revoked_by      VARCHAR(100)          NULL,
    revoked_reason  VARCHAR(255)          NULL,
    ip_address      VARCHAR(45)           NULL,
    user_agent      VARCHAR(500)          NULL,
    device_id       VARCHAR(255)          NULL,
    device_name     VARCHAR(100)          NULL,
    token_family    VARCHAR(100)          NULL,
    parent_token_id BIGINT                NULL,
    last_used_at    datetime              NULL,
    use_count       INT                   NOT NULL,
    CONSTRAINT pk_refresh_token PRIMARY KEY (id)
);

CREATE TABLE reviews
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    deleted_at      datetime              NULL,
    deleted_by      VARCHAR(100)          NULL,
    created_at      datetime              NOT NULL,
    created_by      VARCHAR(100)          NULL,
    updated_at      datetime              NULL,
    updated_by      VARCHAR(100)          NULL,
    dish_id         BIGINT                NOT NULL,
    customer_name   VARCHAR(100)          NOT NULL,
    customer_email  VARCHAR(255)          NULL,
    customer_avatar VARCHAR(500)          NULL,
    rating          INT                   NOT NULL,
    comment         TEXT                  NOT NULL,
    is_active       BIT(1)                NOT NULL,
    is_verified     BIT(1)                NOT NULL,
    ip_address      VARCHAR(45)           NULL,
    CONSTRAINT pk_reviews PRIMARY KEY (id)
);

CREATE TABLE sales_report
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    booking_id  BIGINT                NOT NULL,
    amount      DECIMAL(10, 2)        NOT NULL,
    report_date date                  NOT NULL,
    status      VARCHAR(20)           NOT NULL,
    metadata    TEXT                  NULL,
    created_at  datetime              NOT NULL,
    updated_at  datetime              NULL,
    CONSTRAINT pk_sales_report PRIMARY KEY (id)
);

CREATE TABLE tables
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    deleted_at    datetime              NULL,
    deleted_by    VARCHAR(100)          NULL,
    created_at    datetime              NOT NULL,
    created_by    VARCHAR(100)          NULL,
    updated_at    datetime              NULL,
    updated_by    VARCHAR(100)          NULL,
    table_name    VARCHAR(50)           NOT NULL,
    capacity      INT                   NOT NULL,
    status        VARCHAR(20)           NOT NULL,
    `description` TEXT                  NULL,
    CONSTRAINT pk_tables PRIMARY KEY (id)
);

CREATE TABLE user_analytics
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    user_id          BIGINT                NOT NULL,
    action_type      VARCHAR(50)           NOT NULL,
    action_timestamp datetime              NOT NULL,
    metadata         JSON                  NULL,
    CONSTRAINT pk_user_analytics PRIMARY KEY (id)
);

CREATE TABLE user_roles
(
    id   BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (id)
);

CREATE TABLE users
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    deleted_at   datetime              NULL,
    deleted_by   VARCHAR(100)          NULL,
    created_at   datetime              NOT NULL,
    created_by   VARCHAR(100)          NULL,
    updated_at   datetime              NULL,
    updated_by   VARCHAR(100)          NULL,
    username     VARCHAR(50)           NOT NULL,
    password     VARCHAR(255)          NOT NULL,
    full_name    VARCHAR(100)          NULL,
    email        VARCHAR(100)          NULL,
    phone_number VARCHAR(20)           NULL,
    address      VARCHAR(255)          NULL,
    role_id      BIGINT                NOT NULL,
    status       VARCHAR(20)           NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE daily_statistics
    ADD CONSTRAINT uc_daily_statistics_stat_date UNIQUE (stat_date);

ALTER TABLE outbox_events
    ADD CONSTRAINT uc_outbox_events_event UNIQUE (event_id);

ALTER TABLE payments
    ADD CONSTRAINT uc_payments_transaction_reference UNIQUE (transaction_reference);

ALTER TABLE refresh_token
    ADD CONSTRAINT uc_refresh_token_token UNIQUE (token);

ALTER TABLE tables
    ADD CONSTRAINT uc_tables_table_name UNIQUE (table_name);

ALTER TABLE user_roles
    ADD CONSTRAINT uc_user_roles_name UNIQUE (name);

ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username);

CREATE INDEX idx_booking_id ON preorders (booking_id);

CREATE INDEX idx_capacity ON tables (capacity);

CREATE INDEX idx_customer_email ON reviews (customer_email);

CREATE INDEX idx_is_active ON reviews (is_active);

CREATE INDEX idx_rating ON reviews (rating);

CREATE INDEX idx_refresh_token_deleted_at ON refresh_token (deleted_at);

CREATE INDEX idx_refresh_token_expiry_date ON refresh_token (expiry_date);

CREATE INDEX idx_refresh_token_revoked ON refresh_token (revoked);

CREATE INDEX idx_report_date ON sales_report (report_date);

CREATE INDEX idx_status ON tables (status);

CREATE INDEX idx_status ON tables (status);

CREATE INDEX idx_status ON tables (status);

CREATE INDEX idx_transaction_ref ON payments (transaction_reference);

ALTER TABLE bookings
    ADD CONSTRAINT FK_BOOKINGS_ON_TABLE FOREIGN KEY (table_id) REFERENCES tables (id);

ALTER TABLE bookings
    ADD CONSTRAINT FK_BOOKINGS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE carts
    ADD CONSTRAINT FK_CARTS_ON_DISHID FOREIGN KEY (dish_id) REFERENCES dishes (id);

ALTER TABLE images
    ADD CONSTRAINT FK_IMAGES_ON_DISH FOREIGN KEY (dish_id) REFERENCES dishes (id);

ALTER TABLE images
    ADD CONSTRAINT FK_IMAGES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE notifications
    ADD CONSTRAINT FK_NOTIFICATIONS_ON_TO_USER FOREIGN KEY (to_user_id) REFERENCES users (id);

ALTER TABLE order_history
    ADD CONSTRAINT FK_ORDER_HISTORY_ON_BOOKING FOREIGN KEY (booking_id) REFERENCES bookings (id);

CREATE INDEX idx_booking_id ON preorders (booking_id);

ALTER TABLE order_history
    ADD CONSTRAINT FK_ORDER_HISTORY_ON_DISH FOREIGN KEY (dish_id) REFERENCES dishes (id);

CREATE INDEX idx_dish_id ON reviews (dish_id);

ALTER TABLE order_history
    ADD CONSTRAINT FK_ORDER_HISTORY_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX idx_user_id ON order_history (user_id);

ALTER TABLE payments
    ADD CONSTRAINT FK_PAYMENTS_ON_BOOKING FOREIGN KEY (booking_id) REFERENCES bookings (id);

CREATE INDEX idx_booking_id ON preorders (booking_id);

ALTER TABLE preorders
    ADD CONSTRAINT FK_PREORDERS_ON_BOOKING FOREIGN KEY (booking_id) REFERENCES bookings (id);

CREATE INDEX idx_booking_id ON preorders (booking_id);

ALTER TABLE preorders
    ADD CONSTRAINT FK_PREORDERS_ON_DISH FOREIGN KEY (dish_id) REFERENCES dishes (id);

CREATE INDEX idx_dish_id ON reviews (dish_id);

ALTER TABLE refresh_token
    ADD CONSTRAINT FK_REFRESH_TOKEN_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX idx_refresh_token_user_id ON refresh_token (user_id);

ALTER TABLE reviews
    ADD CONSTRAINT FK_REVIEWS_ON_DISH FOREIGN KEY (dish_id) REFERENCES dishes (id);

CREATE INDEX idx_dish_id ON reviews (dish_id);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_ROLE FOREIGN KEY (role_id) REFERENCES user_roles (id);