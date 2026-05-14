-- ============================================================
-- LabourHand Database Schema
-- Generated from JPA entity models + API test collection
-- Compatible with MySQL 8.x and MariaDB 10.x+
-- ============================================================

CREATE DATABASE IF NOT EXISTS labourhand_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE labourhand_db;

-- ============================================================
-- 1. SKILLS
-- ============================================================
CREATE TABLE IF NOT EXISTS skills (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 2. USERS
-- Role: WORKER | OWNER
-- Language: en | hi
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    phone         VARCHAR(20)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('WORKER','OWNER') NOT NULL,
    avatar        VARCHAR(1000),
    verified      TINYINT(1)   NOT NULL DEFAULT 0,
    language      ENUM('en','hi')       DEFAULT 'en',
    created_at    DATETIME              DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_role (role),
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 3. WORKER PROFILES  (1-to-1 with users where role=WORKER)
-- ============================================================
CREATE TABLE IF NOT EXISTS worker_profiles (
    user_id              BIGINT PRIMARY KEY,
    specialization       VARCHAR(255),
    years_experience     INT            DEFAULT 0,
    rating               DOUBLE         DEFAULT 0.0,
    completed_jobs       INT            DEFAULT 0,
    bio                  TEXT,
    skills_india_verified TINYINT(1)   DEFAULT 0,
    on_time_rate         DOUBLE         DEFAULT 0.0,
    rehire_rate          DOUBLE         DEFAULT 0.0,
    status               VARCHAR(50)    DEFAULT 'available',   -- available | on-site
    current_site         VARCHAR(255),
    -- Payment details
    payment_method       VARCHAR(10),    -- BANK | UPI
    bank_account_no      VARCHAR(50),
    bank_name            VARCHAR(100),
    ifsc_code            VARCHAR(20),
    holder_name          VARCHAR(255),
    upi_id               VARCHAR(100),
    CONSTRAINT fk_wp_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 4. OWNER PROFILES  (1-to-1 with users where role=OWNER)
-- ============================================================
CREATE TABLE IF NOT EXISTS owner_profiles (
    user_id          BIGINT PRIMARY KEY,
    company_name     VARCHAR(255),
    projects_posted  INT DEFAULT 0,
    CONSTRAINT fk_op_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 5. WORKER SKILLS  (Many-to-Many: worker_profiles <-> skills)
-- ============================================================
CREATE TABLE IF NOT EXISTS worker_skills (
    worker_id BIGINT NOT NULL,
    skill_id  BIGINT NOT NULL,
    PRIMARY KEY (worker_id, skill_id),
    CONSTRAINT fk_ws_worker FOREIGN KEY (worker_id) REFERENCES worker_profiles(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_ws_skill  FOREIGN KEY (skill_id)  REFERENCES skills(id)          ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 6. CERTIFICATIONS  (Many-to-One: certifications -> worker_profiles)
-- ============================================================
CREATE TABLE IF NOT EXISTS certifications (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    worker_id BIGINT       NOT NULL,
    name      VARCHAR(255) NOT NULL,
    issuer    VARCHAR(255),
    year      VARCHAR(10),
    CONSTRAINT fk_cert_worker FOREIGN KEY (worker_id) REFERENCES worker_profiles(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 7. PROJECTS
-- Status: OPEN_FOR_BIDS | IN_PROGRESS | COMPLETED | PAYMENT_VERIFIED
-- ============================================================
CREATE TABLE IF NOT EXISTS projects (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    category        VARCHAR(100),          -- Masonry, Painting, Electrical, Plumbing, Carpentry, Construction
    budget          BIGINT       NOT NULL,  -- amount in INR
    timeline_days   INT          DEFAULT 0,
    location        VARCHAR(255) NOT NULL,
    lat             DOUBLE,
    lng             DOUBLE,
    status          ENUM('OPEN_FOR_BIDS','IN_PROGRESS','COMPLETED','PAYMENT_VERIFIED')
                    NOT NULL DEFAULT 'OPEN_FOR_BIDS',
    owner_id        BIGINT       NOT NULL,
    accepted_bid_id BIGINT,                -- set after bid acceptance
    progress        INT          DEFAULT 0, -- 0-100 %
    deadline        DATE,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_proj_owner  (owner_id),
    INDEX idx_proj_status (status),
    INDEX idx_proj_latlng (lat, lng)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 8. BIDS
-- Status: PENDING | ACCEPTED | REJECTED
-- ============================================================
CREATE TABLE IF NOT EXISTS bids (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id     BIGINT    NOT NULL,
    worker_id      BIGINT    NOT NULL,
    amount         BIGINT    NOT NULL,       -- bid amount in INR
    amount_paid    BIGINT    NOT NULL DEFAULT 0, -- portion already paid
    estimated_days INT       NOT NULL,
    message        TEXT,
    status         ENUM('PENDING','ACCEPTED','REJECTED') NOT NULL DEFAULT 'PENDING',
    recommended    TINYINT(1) DEFAULT 0,
    submitted_at   DATETIME  DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_bids_project (project_id),
    INDEX idx_bids_worker  (worker_id),
    INDEX idx_bids_status  (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 9. BID TEAM WORKERS  (@ElementCollection on Bid.teamWorkerIds)
-- ============================================================
CREATE TABLE IF NOT EXISTS bid_team_workers (
    bid_id    BIGINT NOT NULL,
    worker_id BIGINT NOT NULL,
    PRIMARY KEY (bid_id, worker_id),
    CONSTRAINT fk_btw_bid FOREIGN KEY (bid_id) REFERENCES bids(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 10. PROGRESS UPDATES
-- ============================================================
CREATE TABLE IF NOT EXISTS progress_updates (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id          BIGINT  NOT NULL,
    user_id             BIGINT  NOT NULL,    -- Worker or Employer posting the update
    progress_percentage INT,                 -- e.g. 25 = 25%
    comment             TEXT,
    payment_demanded    TINYINT(1),
    demand_amount       DOUBLE,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_pu_project (project_id),
    INDEX idx_pu_user    (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 11. PAYMENTS
-- Method: ONLINE | CASH
-- Status: PENDING | COMPLETED | FAILED
-- ============================================================
CREATE TABLE IF NOT EXISTS payments (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id         BIGINT  NOT NULL,
    progress_update_id BIGINT,              -- update that triggered this demand
    payer_id           BIGINT  NOT NULL,    -- Employer
    payee_id           BIGINT  NOT NULL,    -- Worker
    amount             DOUBLE  NOT NULL,
    payment_method     ENUM('ONLINE','CASH') NOT NULL,
    status             ENUM('PENDING','COMPLETED','FAILED') NOT NULL,
    reference_id       VARCHAR(255),        -- Razorpay ID or cash receipt note
    created_at         DATETIME DEFAULT CURRENT_TIMESTAMP,
    paid_at            DATETIME,
    INDEX idx_pay_project (project_id),
    INDEX idx_pay_payer   (payer_id),
    INDEX idx_pay_payee   (payee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 12. CONVERSATIONS
-- ============================================================
CREATE TABLE IF NOT EXISTS conversations (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    participant1_id BIGINT NOT NULL,
    participant2_id BIGINT NOT NULL,
    project_id      BIGINT,                 -- context project (nullable)
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_conv_p1 (participant1_id),
    INDEX idx_conv_p2 (participant2_id),
    UNIQUE KEY uq_conversation (participant1_id, participant2_id, project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 13. MESSAGES
-- ============================================================
CREATE TABLE IF NOT EXISTS messages (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT    NOT NULL,
    sender_id       BIGINT    NOT NULL,
    content         TEXT      NOT NULL,
    sent_at         DATETIME  DEFAULT CURRENT_TIMESTAMP,
    is_read         TINYINT(1) DEFAULT 0,
    INDEX idx_msg_conversation (conversation_id),
    INDEX idx_msg_sender       (sender_id),
    CONSTRAINT fk_msg_conv FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 14. REVIEWS
-- ============================================================
CREATE TABLE IF NOT EXISTS reviews (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id   BIGINT  NOT NULL,
    reviewer_id  BIGINT  NOT NULL,    -- Owner who reviews
    reviewee_id  BIGINT  NOT NULL,    -- Worker being reviewed
    rating       DOUBLE  NOT NULL,    -- 1.0 - 5.0
    comment      TEXT,
    duration     VARCHAR(100),        -- e.g. "12 Days"
    project_name VARCHAR(255),
    client_name  VARCHAR(255),
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_rev_project  (project_id),
    INDEX idx_rev_reviewee (reviewee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 15. EARNINGS  (owner revenue tracking per project per day)
-- ============================================================
CREATE TABLE IF NOT EXISTS earnings (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id   BIGINT NOT NULL,
    project_id BIGINT,
    amount     BIGINT NOT NULL,    -- in INR
    date       DATE   NOT NULL,
    INDEX idx_earn_owner   (owner_id),
    INDEX idx_earn_project (project_id),
    INDEX idx_earn_date    (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 16. SCHEDULE EVENTS  (contractor scheduling)
-- ============================================================
CREATE TABLE IF NOT EXISTS schedule_events (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    worker_id  BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    date       DATE   NOT NULL,
    start_time TIME,
    end_time   TIME,
    INDEX idx_se_worker  (worker_id),
    INDEX idx_se_project (project_id),
    INDEX idx_se_date    (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 17. RATINGS  (separate from reviews, for numeric rating tracking)
-- ============================================================
CREATE TABLE IF NOT EXISTS ratings (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_user_id  BIGINT  NOT NULL,
    to_user_id    BIGINT  NOT NULL,
    project_id    BIGINT  NOT NULL,
    rating        DOUBLE  NOT NULL,    -- 1.0 - 5.0
    comment       TEXT,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_rating_rated   (to_user_id),
    INDEX idx_rating_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Foreign key cross-references (added after all tables exist)
-- ============================================================
ALTER TABLE projects
    ADD CONSTRAINT fk_proj_owner
        FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE projects
    ADD CONSTRAINT fk_proj_accepted_bid
        FOREIGN KEY (accepted_bid_id) REFERENCES bids(id) ON DELETE SET NULL;

ALTER TABLE bids
    ADD CONSTRAINT fk_bid_project
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE;

ALTER TABLE bids
    ADD CONSTRAINT fk_bid_worker
        FOREIGN KEY (worker_id) REFERENCES worker_profiles(user_id) ON DELETE CASCADE;

ALTER TABLE payments
    ADD CONSTRAINT fk_pay_project
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE;

ALTER TABLE payments
    ADD CONSTRAINT fk_pay_progress_update
        FOREIGN KEY (progress_update_id) REFERENCES progress_updates(id) ON DELETE SET NULL;

ALTER TABLE progress_updates
    ADD CONSTRAINT fk_pu_project
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE;

ALTER TABLE earnings
    ADD CONSTRAINT fk_earn_owner
        FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE schedule_events
    ADD CONSTRAINT fk_se_worker
        FOREIGN KEY (worker_id) REFERENCES worker_profiles(user_id) ON DELETE CASCADE;

ALTER TABLE schedule_events
    ADD CONSTRAINT fk_se_project
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE;

ALTER TABLE reviews
    ADD CONSTRAINT fk_rev_project
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE;

ALTER TABLE ratings
    ADD CONSTRAINT fk_rat_project
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE;
