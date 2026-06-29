-- ========== 话题讨论系统 ==========

-- 话题表
CREATE TABLE IF NOT EXISTS topic (
    topic_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id   BIGINT      NOT NULL,
    user_id     BIGINT      NOT NULL,
    title       VARCHAR(200) NOT NULL,
    content     TEXT,
    is_pinned   TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否置顶',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_course (course_id),
    INDEX idx_user   (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 回复表
CREATE TABLE IF NOT EXISTS reply (
    reply_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic_id    BIGINT      NOT NULL,
    user_id     BIGINT      COMMENT 'NULL 表示匿名回复',
    content     TEXT        NOT NULL,
    is_anonymous TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否匿名',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_topic (topic_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 课程表新增：是否禁止讨论
ALTER TABLE course ADD COLUMN discussion_locked TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否禁止学生讨论';
