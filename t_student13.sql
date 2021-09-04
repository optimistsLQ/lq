/*
Navicat MySQL Data Transfer

Source Server         : MySQL111
Source Server Version : 80026
Source Host           : localhost:3306
Source Database       : demo

Target Server Type    : MYSQL
Target Server Version : 80026
File Encoding         : 65001

Date: 2021-09-02 22:04:56
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_student13
-- ----------------------------
DROP TABLE IF EXISTS `t_student13`;
CREATE TABLE `t_student13` (
  `id` int NOT NULL AUTO_INCREMENT,
  `stu_name` varchar(20) DEFAULT NULL,
  `age` int DEFAULT NULL,
  `sex` varchar(6) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Records of t_student13
-- ----------------------------
INSERT INTO `t_student13` VALUES ('1', 'zhangsan', '25', '男', '2021-09-02 22:02:17');
INSERT INTO `t_student13` VALUES ('2', '李四', '28', '女', '2021-09-02 22:02:43');
INSERT INTO `t_student13` VALUES ('3', '王五', '30', '女', '2021-09-02 22:03:03');
INSERT INTO `t_student13` VALUES ('4', '赵六', '21', '男', '2021-09-02 22:03:30');
