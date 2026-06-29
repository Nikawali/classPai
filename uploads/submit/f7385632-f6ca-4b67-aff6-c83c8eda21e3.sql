/*
 Navicat Premium Dump SQL

 Source Server         : data1
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : classpai

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 25/06/2026 21:33:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for course
-- ----------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course`  (
  `course_id` bigint NOT NULL AUTO_INCREMENT COMMENT '课程id',
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程名',
  `course_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `course_intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '课程简介',
  `start_date` date NOT NULL COMMENT '开课日期',
  `end_date` date NOT NULL COMMENT '结课日期',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `semester` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`course_id`) USING BTREE,
  UNIQUE INDEX `course_code`(`course_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course
-- ----------------------------
INSERT INTO `course` VALUES (4, '数学', '874DSM', '111', '2020-09-01', '2021-06-30', '2026-06-25 18:46:27', '第一学期');
INSERT INTO `course` VALUES (5, '英语', '8ZHT7A', '11', '2021-09-01', '2022-06-30', '2026-06-25 19:20:41', '第二学期');
INSERT INTO `course` VALUES (6, '数据结构', 'SPE2DW', '软工3、4班', '2024-09-01', '2025-06-30', '2026-06-25 19:35:59', '第二学期');
INSERT INTO `course` VALUES (7, '计算机组成原理', 'HMP93F', '软工3、4班', '2025-09-01', '2026-06-30', '2026-06-25 19:36:42', '第二学期');
INSERT INTO `course` VALUES (8, '高级java', 'RWY896', '软工3、4班', '2024-09-01', '2025-06-30', '2026-06-25 19:37:06', '第一学期');
INSERT INTO `course` VALUES (9, '离散数学', '9PDZ8U', '软工3、4班', '2024-09-01', '2025-06-30', '2026-06-25 20:11:28', '第一学期');
INSERT INTO `course` VALUES (10, '线性代数', 'XVLQZG', '软工3、4班', '2024-09-01', '2025-06-30', '2026-06-25 21:07:51', '第二学期');

-- ----------------------------
-- Table structure for homework
-- ----------------------------
DROP TABLE IF EXISTS `homework`;
CREATE TABLE `homework`  (
  `hw_id` bigint NOT NULL AUTO_INCREMENT COMMENT '作业主键',
  `course_id` bigint NOT NULL COMMENT '所属课程',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作业标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '作业文字描述、要求',
  `deadline` datetime NULL DEFAULT NULL COMMENT '截止提交时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`hw_id`) USING BTREE,
  INDEX `course_id`(`course_id` ASC) USING BTREE,
  CONSTRAINT `homework_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程作业表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of homework
-- ----------------------------

-- ----------------------------
-- Table structure for homework_file
-- ----------------------------
DROP TABLE IF EXISTS `homework_file`;
CREATE TABLE `homework_file`  (
  `file_id` bigint NOT NULL AUTO_INCREMENT,
  `hw_id` bigint NOT NULL COMMENT '归属哪条作业',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '服务器存储路径/oss地址',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小字节',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '后缀pdf/word/图片',
  `upload_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`file_id`) USING BTREE,
  INDEX `hw_id`(`hw_id` ASC) USING BTREE,
  CONSTRAINT `homework_file_ibfk_1` FOREIGN KEY (`hw_id`) REFERENCES `homework` (`hw_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '作业老师附件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of homework_file
-- ----------------------------

-- ----------------------------
-- Table structure for homework_submit
-- ----------------------------
DROP TABLE IF EXISTS `homework_submit`;
CREATE TABLE `homework_submit`  (
  `submit_id` bigint NOT NULL AUTO_INCREMENT,
  `hw_id` bigint NOT NULL COMMENT '提交对应作业',
  `student_id` bigint NOT NULL COMMENT '提交学生',
  `submit_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文字作答内容',
  `submit_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `score` decimal(5, 1) NULL DEFAULT NULL COMMENT '老师批改分数',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '老师批改评语',
  PRIMARY KEY (`submit_id`) USING BTREE,
  UNIQUE INDEX `uk_hw_student`(`hw_id` ASC, `student_id` ASC) USING BTREE,
  INDEX `student_id`(`student_id` ASC) USING BTREE,
  CONSTRAINT `homework_submit_ibfk_1` FOREIGN KEY (`hw_id`) REFERENCES `homework` (`hw_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生作业提交记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of homework_submit
-- ----------------------------

-- ----------------------------
-- Table structure for school
-- ----------------------------
DROP TABLE IF EXISTS `school`;
CREATE TABLE `school`  (
  `school_id` bigint NOT NULL AUTO_INCREMENT,
  `school_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`school_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 69 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of school
-- ----------------------------
INSERT INTO `school` VALUES (1, '清华大学');
INSERT INTO `school` VALUES (2, '北京大学');
INSERT INTO `school` VALUES (3, '浙江大学');
INSERT INTO `school` VALUES (4, '复旦大学');
INSERT INTO `school` VALUES (5, '上海交通大学');
INSERT INTO `school` VALUES (6, '南京大学');
INSERT INTO `school` VALUES (7, '武汉大学');
INSERT INTO `school` VALUES (8, '华中科技大学');
INSERT INTO `school` VALUES (9, '中国人民大学');
INSERT INTO `school` VALUES (10, '北京航空航天大学');
INSERT INTO `school` VALUES (11, '北京理工大学');
INSERT INTO `school` VALUES (12, '北京师范大学');
INSERT INTO `school` VALUES (13, '中国农业大学');
INSERT INTO `school` VALUES (14, '南开大学');
INSERT INTO `school` VALUES (15, '天津大学');
INSERT INTO `school` VALUES (16, '大连理工大学');
INSERT INTO `school` VALUES (17, '吉林大学');
INSERT INTO `school` VALUES (18, '哈尔滨工业大学');
INSERT INTO `school` VALUES (19, '同济大学');
INSERT INTO `school` VALUES (20, '华东师范大学');
INSERT INTO `school` VALUES (21, '东南大学');
INSERT INTO `school` VALUES (22, '中国科学技术大学');
INSERT INTO `school` VALUES (23, '厦门大学');
INSERT INTO `school` VALUES (24, '山东大学');
INSERT INTO `school` VALUES (25, '中国海洋大学');
INSERT INTO `school` VALUES (26, '中南大学');
INSERT INTO `school` VALUES (27, '中山大学');
INSERT INTO `school` VALUES (28, '华南理工大学');
INSERT INTO `school` VALUES (29, '四川大学');
INSERT INTO `school` VALUES (30, '电子科技大学');
INSERT INTO `school` VALUES (31, '重庆大学');
INSERT INTO `school` VALUES (32, '西安交通大学');
INSERT INTO `school` VALUES (33, '西北工业大学');
INSERT INTO `school` VALUES (34, '兰州大学');
INSERT INTO `school` VALUES (35, '国防科技大学');
INSERT INTO `school` VALUES (36, '北京邮电大学');
INSERT INTO `school` VALUES (37, '中央财经大学');
INSERT INTO `school` VALUES (38, '对外经济贸易大学');
INSERT INTO `school` VALUES (39, '中国政法大学');
INSERT INTO `school` VALUES (40, '上海财经大学');
INSERT INTO `school` VALUES (41, '南京航空航天大学');
INSERT INTO `school` VALUES (42, '南京理工大学');
INSERT INTO `school` VALUES (43, '西安电子科技大学');
INSERT INTO `school` VALUES (44, '北京交通大学');
INSERT INTO `school` VALUES (45, '北京科技大学');
INSERT INTO `school` VALUES (46, '北京化工大学');
INSERT INTO `school` VALUES (47, '北京工业大学');
INSERT INTO `school` VALUES (48, '华北电力大学');
INSERT INTO `school` VALUES (49, '东北大学');
INSERT INTO `school` VALUES (50, '华东理工大学');
INSERT INTO `school` VALUES (51, '东华大学');
INSERT INTO `school` VALUES (52, '上海大学');
INSERT INTO `school` VALUES (53, '苏州大学');
INSERT INTO `school` VALUES (54, '合肥工业大学');
INSERT INTO `school` VALUES (55, '中国地质大学(武汉)');
INSERT INTO `school` VALUES (56, '武汉理工大学');
INSERT INTO `school` VALUES (57, '湖南大学');
INSERT INTO `school` VALUES (58, '暨南大学');
INSERT INTO `school` VALUES (59, '西南交通大学');
INSERT INTO `school` VALUES (60, '西南财经大学');
INSERT INTO `school` VALUES (61, '西北大学');
INSERT INTO `school` VALUES (62, '郑州大学');
INSERT INTO `school` VALUES (63, '南京师范大学');
INSERT INTO `school` VALUES (64, '南昌大学');
INSERT INTO `school` VALUES (65, '福州大学');
INSERT INTO `school` VALUES (66, '云南大学');
INSERT INTO `school` VALUES (67, '广西大学');
INSERT INTO `school` VALUES (68, '深圳大学');

-- ----------------------------
-- Table structure for submit_file
-- ----------------------------
DROP TABLE IF EXISTS `submit_file`;
CREATE TABLE `submit_file`  (
  `s_file_id` bigint NOT NULL AUTO_INCREMENT,
  `submit_id` bigint NOT NULL COMMENT '归属一条提交记录',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_size` bigint NULL DEFAULT NULL,
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `upload_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`s_file_id`) USING BTREE,
  INDEX `submit_id`(`submit_id` ASC) USING BTREE,
  CONSTRAINT `submit_file_ibfk_1` FOREIGN KEY (`submit_id`) REFERENCES `homework_submit` (`submit_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生提交附件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of submit_file
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `phone` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `school` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `college` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `major` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`user_name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('汪旭', '$2a$10$AU7lGWvXRQO45GD2s4KnuejVCS1jPBmBeOO1CVqlzYJIAEb3WwqV.', '13996661380', '2026-06-24 11:10:26', 12423020421, 'male', '上海交通大学', 'student', '人工智能学院', '软件工程');
INSERT INTO `user` VALUES ('qqq', '$2a$10$Cb1Be2ZtvdJoXfCL3IW2..KMuSu5x.J1eBnkmANjCCZJEbFgrfDZ6', '13996661382', '2026-06-25 18:48:23', 12423020422, 'male', '上海交通大学', 'student', '', '');

-- ----------------------------
-- Table structure for user_course
-- ----------------------------
DROP TABLE IF EXISTS `user_course`;
CREATE TABLE `user_course`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `role` enum('teacher','student') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `join_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `pinned` tinyint(1) NULL DEFAULT 0 COMMENT '是否置顶',
  `sort_order` int NULL DEFAULT NULL COMMENT '置顶位置',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC, `course_id` ASC) USING BTREE,
  INDEX `course_id`(`course_id` ASC) USING BTREE,
  CONSTRAINT `user_course_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `user_course_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_course
-- ----------------------------
INSERT INTO `user_course` VALUES (4, 12423020421, 4, 'teacher', '2026-06-25 18:46:27', 1, 1);
INSERT INTO `user_course` VALUES (5, 12423020422, 4, 'student', '2026-06-25 18:46:27', 1, 2);
INSERT INTO `user_course` VALUES (6, 12423020422, 5, 'teacher', '2026-06-25 19:20:41', 1, 4);
INSERT INTO `user_course` VALUES (7, 12423020422, 6, 'teacher', '2026-06-25 19:35:59', 1, 5);
INSERT INTO `user_course` VALUES (8, 12423020422, 7, 'teacher', '2026-06-25 19:36:42', 1, 1);
INSERT INTO `user_course` VALUES (9, 12423020422, 8, 'teacher', '2026-06-25 19:37:06', 0, 3);
INSERT INTO `user_course` VALUES (10, 12423020422, 9, 'teacher', '2026-06-25 20:11:28', 0, NULL);
INSERT INTO `user_course` VALUES (11, 12423020422, 10, 'teacher', '2026-06-25 21:07:51', 1, 6);
INSERT INTO `user_course` VALUES (12, 12423020421, 7, 'student', '2026-06-25 21:14:47', 1, 2);

SET FOREIGN_KEY_CHECKS = 1;
