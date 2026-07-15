-- H2 兼容的初始化脚本（无 CREATE DATABASE / USE）

CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(64)  NOT NULL UNIQUE,
    password    VARCHAR(256) NOT NULL,
    role        VARCHAR(16)  NOT NULL,
    nickname    VARCHAR(64)  DEFAULT NULL,
    email       VARCHAR(128) DEFAULT NULL,
    phone       VARCHAR(20)  DEFAULT NULL,
    status      TINYINT      DEFAULT 1,
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS resume (
    id                 BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id            BIGINT       NOT NULL,
    file_name          VARCHAR(256) DEFAULT NULL,
    storage_key        VARCHAR(128) DEFAULT NULL,
    status             TINYINT      DEFAULT 0,
    raw_text           MEDIUMTEXT   DEFAULT NULL,
    parsed_json        MEDIUMTEXT   DEFAULT NULL,
    ability_profile    TEXT         DEFAULT NULL,
    skills             TEXT         DEFAULT NULL,
    years_of_experience INT         DEFAULT NULL,
    embedding          TEXT         DEFAULT NULL,
    create_time        DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time        DATETIME     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS job (
    id                   BIGINT       AUTO_INCREMENT PRIMARY KEY,
    hr_id                BIGINT       NOT NULL,
    title                VARCHAR(128) NOT NULL,
    company              VARCHAR(128) DEFAULT NULL,
    location             VARCHAR(128) DEFAULT NULL,
    description          TEXT         DEFAULT NULL,
    requirement_tags     TEXT         DEFAULT NULL,
    embedding            TEXT         DEFAULT NULL,
    salary_min           INT          DEFAULT NULL,
    salary_max           INT          DEFAULT NULL,
    status               TINYINT      DEFAULT 1,
    create_time          DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time          DATETIME     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS evaluation_result (
    id               BIGINT       AUTO_INCREMENT PRIMARY KEY,
    resume_id        BIGINT       NOT NULL,
    job_id           BIGINT       NOT NULL,
    user_id          BIGINT       NOT NULL,
    skill_match      INT          DEFAULT 0,
    experience_match INT          DEFAULT 0,
    education_match  INT          DEFAULT 0,
    overall          INT          DEFAULT 0,
    strengths        TEXT         DEFAULT NULL,
    gaps             TEXT         DEFAULT NULL,
    comment          TEXT         DEFAULT NULL,
    raw_json         TEXT         DEFAULT NULL,
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS recommendation (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    resume_id   BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    job_id      BIGINT       NOT NULL,
    match_score INT          DEFAULT 0,
    reason_type VARCHAR(32)  DEFAULT NULL,
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS application (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    resume_id   BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    job_id      BIGINT       NOT NULL,
    status      TINYINT      DEFAULT 0,
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP
);
