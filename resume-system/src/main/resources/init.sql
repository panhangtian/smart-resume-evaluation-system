-- ============================================================
-- 智能简历评估与岗位推荐系统 - 数据库初始化脚本
-- MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS resume_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE resume_system;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(64)  NOT NULL UNIQUE COMMENT '登录账号',
    password    VARCHAR(256) NOT NULL COMMENT 'BCrypt 加密密码',
    role        VARCHAR(16)  NOT NULL COMMENT 'JOBSEEKER / HR / ADMIN',
    nickname    VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    email       VARCHAR(128) DEFAULT NULL,
    phone       VARCHAR(20)  DEFAULT NULL,
    status      TINYINT      DEFAULT 1 COMMENT '1启用 0禁用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

-- 简历表
CREATE TABLE IF NOT EXISTS resume (
    id                 BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id            BIGINT       NOT NULL COMMENT '所属求职者',
    file_name          VARCHAR(256) DEFAULT NULL COMMENT '原始文件名',
    storage_key        VARCHAR(128) DEFAULT NULL COMMENT '存储key',
    status             TINYINT      DEFAULT 0 COMMENT '0待解析 1解析中 2已解析 3解析失败',
    raw_text           MEDIUMTEXT   DEFAULT NULL COMMENT '提取的纯文本',
    parsed_json        MEDIUMTEXT   DEFAULT NULL COMMENT 'AI解析的结构化JSON',
    ability_profile    TEXT         DEFAULT NULL COMMENT '能力画像',
    skills             TEXT         DEFAULT NULL COMMENT '技能标签(逗号分隔)',
    years_of_experience INT        DEFAULT NULL COMMENT '工作年限',
    embedding          TEXT         DEFAULT NULL COMMENT '向量(JSON数组)',
    create_time        DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time        DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历';

-- 岗位表
CREATE TABLE IF NOT EXISTS job (
    id                   BIGINT       AUTO_INCREMENT PRIMARY KEY,
    hr_id                BIGINT       NOT NULL COMMENT '发布HR',
    title                VARCHAR(128) NOT NULL COMMENT '岗位名称',
    company              VARCHAR(128) DEFAULT NULL COMMENT '公司名称',
    location             VARCHAR(128) DEFAULT NULL COMMENT '工作地点',
    description          TEXT         DEFAULT NULL COMMENT '岗位描述',
    requirement_tags     TEXT         DEFAULT NULL COMMENT '技能/要求标签(逗号分隔)',
    embedding            TEXT         DEFAULT NULL COMMENT '岗位向量(JSON数组)',
    salary_min           INT          DEFAULT NULL COMMENT '薪资下限(K)',
    salary_max           INT          DEFAULT NULL COMMENT '薪资上限(K)',
    status               TINYINT      DEFAULT 1 COMMENT '1已发布 0下架',
    create_time          DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time          DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_hr_id (hr_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位';

-- 评估结果表
CREATE TABLE IF NOT EXISTS evaluation_result (
    id               BIGINT       AUTO_INCREMENT PRIMARY KEY,
    resume_id        BIGINT       NOT NULL COMMENT '简历ID',
    job_id           BIGINT       NOT NULL COMMENT '岗位ID',
    user_id          BIGINT       NOT NULL COMMENT '求职者ID',
    skill_match      INT          DEFAULT 0 COMMENT '技能匹配分(0-100)',
    experience_match INT          DEFAULT 0 COMMENT '经验匹配分',
    education_match  INT          DEFAULT 0 COMMENT '学历匹配分',
    overall          INT          DEFAULT 0 COMMENT '综合分',
    strengths        TEXT         DEFAULT NULL COMMENT '优势(JSON数组)',
    gaps             TEXT         DEFAULT NULL COMMENT '短板(JSON数组)',
    comment          TEXT         DEFAULT NULL COMMENT '一句话总评',
    raw_json         TEXT         DEFAULT NULL COMMENT 'AI原始输出',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_resume_id (resume_id),
    INDEX idx_job_id (job_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估结果';

-- 推荐记录表
CREATE TABLE IF NOT EXISTS recommendation (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    resume_id   BIGINT       NOT NULL COMMENT '简历ID',
    user_id     BIGINT       NOT NULL COMMENT '求职者ID',
    job_id      BIGINT       NOT NULL COMMENT '推荐岗位ID',
    match_score INT          DEFAULT 0 COMMENT '匹配度(0-100)',
    reason_type VARCHAR(32)  DEFAULT NULL COMMENT '推荐理由类型(skill/experience/hybrid)',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_job_id (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐记录';

-- 投递记录表
CREATE TABLE IF NOT EXISTS application (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    resume_id   BIGINT       NOT NULL COMMENT '简历ID',
    user_id     BIGINT       NOT NULL COMMENT '求职者ID',
    job_id      BIGINT       NOT NULL COMMENT '岗位ID',
    status      TINYINT      DEFAULT 0 COMMENT '0待处理 1已查看 2面试 3通过 4不通过',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_job_id (job_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投递记录';
