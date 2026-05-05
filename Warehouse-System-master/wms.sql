/*
 Navicat Premium Data Transfer

 Source Server         : Mybatis
 Source Server Type    : MySQL
 Source Server Version : 80028
 Source Host           : localhost:3306
 Source Schema         : wms

 Target Server Type    : MySQL
 Target Server Version : 80028
 File Encoding         : 65001

 Date: 06/01/2023 23:05:16
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '货名',
  `storage` int(0) NOT NULL COMMENT '仓库',
  `goodsType` int(0) NOT NULL COMMENT '分类',
  `count` int(0) NULL DEFAULT NULL COMMENT '数量',
  `remark` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods
-- ----------------------------
INSERT INTO `goods` VALUES (1, 'iPhone14', 2, 2, 100, '货物不可以挤压');
INSERT INTO `goods` VALUES (4, '洁面乳', 1, 1, 1000, '货物不可以挤压');
INSERT INTO `goods` VALUES (5, '葡萄', 5, 5, 500, '货物不可以挤压');
INSERT INTO `goods` VALUES (6, '西红柿', 5, 6, 800, '货物不可以挤压');
INSERT INTO `goods` VALUES (7, '皮皮虾', 4, 4, 500, '货物不可以挤压');
INSERT INTO `goods` VALUES (8, 'AD钙', 3, 3, 400, '货物不可以挤压');
INSERT INTO `goods` VALUES (11, 'iPad Air5', 2, 2, 800, '货物不可以挤压');

-- ----------------------------
-- Table structure for goodstype
-- ----------------------------
DROP TABLE IF EXISTS `goodstype`;
CREATE TABLE `goodstype`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '分类名',
  `remark` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goodstype
-- ----------------------------
INSERT INTO `goodstype` VALUES (1, '日用品', '日常生活用品');
INSERT INTO `goodstype` VALUES (2, '数码产品', '数码产品');
INSERT INTO `goodstype` VALUES (3, '食品', '食品');
INSERT INTO `goodstype` VALUES (4, '冷冻品', '冷冻食品');
INSERT INTO `goodstype` VALUES (5, '水果', '水果产品');
INSERT INTO `goodstype` VALUES (6, '蔬菜', '蔬菜产品');

-- ----------------------------
-- Table structure for menu
-- ----------------------------
DROP TABLE IF EXISTS `menu`;
CREATE TABLE `menu`  (
  `id` int(0) NOT NULL,
  `menuCode` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '菜单编码',
  `menuName` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '菜单名字',
  `menuLevel` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '菜单级别',
  `menuParentCode` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '菜单的父code',
  `menuClick` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '点击触发的函数',
  `menuRight` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '权限 0超级管理员，1表示管理员，2表示普通用户，可以用逗号组合使用',
  `menuComponent` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `menuIcon` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of menu
-- ----------------------------
INSERT INTO `menu` VALUES (1, '001', '管理员管理', '1', NULL, 'Admin', '0', 'admin/AdminManage.vue', 'el-icon-s-custom');
INSERT INTO `menu` VALUES (2, '002', '用户管理', '1', NULL, 'User', '0,1', 'user/UserManage.vue', 'el-icon-user-solid');
INSERT INTO `menu` VALUES (3, '003', '仓库管理', '1', NULL, 'Storage', '0,1', 'storage/StorageManage', 'el-icon-office-building');
INSERT INTO `menu` VALUES (4, '004', '物品分类管理', '1', NULL, 'Goodstype', '0,1', 'goodstype/GoodstypeManage', 'el-icon-menu');
INSERT INTO `menu` VALUES (5, '005', '物品管理 ', '1', NULL, 'Goods', '0,1,2', 'goods/GoodsManage', 'el-icon-s-management');
INSERT INTO `menu` VALUES (6, '006', '记录管理', '1', NULL, 'Record', '0,1,2', 'record/RecordManage', 'el-icon-s-order');

-- ----------------------------
-- Table structure for record
-- ----------------------------
DROP TABLE IF EXISTS `record`;
CREATE TABLE `record`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `goods` int(0) NOT NULL COMMENT '货品id',
  `userId` int(0) NULL DEFAULT NULL COMMENT '取货人/补货人',
  `admin_id` int(0) NULL DEFAULT NULL COMMENT '操作人id',
  `count` int(0) NULL DEFAULT NULL COMMENT '数量',
  `createtime` datetime(0) NULL DEFAULT NULL COMMENT '操作时间',
  `remark` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of record
-- ----------------------------
INSERT INTO `record` VALUES (1, 1, 3, 2, 100, '2023-01-06 20:46:48', '取货');
INSERT INTO `record` VALUES (2, 4, 9, 8, 200, '2023-01-06 20:47:45', '补货');
INSERT INTO `record` VALUES (3, 5, 4, 2, 80, '2023-01-06 20:48:47', '取货');
INSERT INTO `record` VALUES (4, 6, 5, 8, 120, '2023-01-06 20:49:57', '补货');
INSERT INTO `record` VALUES (5, 11, 6, 2, 50, '2023-01-06 20:50:20', '取货');
INSERT INTO `record` VALUES (6, 7, 9, 1, 200, NULL, '入库200只皮皮虾');
INSERT INTO `record` VALUES (9, 8, 5, 1, 300, NULL, '入库300箱AD钙');
INSERT INTO `record` VALUES (10, 8, 5, 1, -100, NULL, '出库100箱AD钙');
INSERT INTO `record` VALUES (11, 11, 4, 2, -200, NULL, '出库iPad Air5 200台');

-- ----------------------------
-- Table structure for storage
-- ----------------------------
DROP TABLE IF EXISTS `storage`;
CREATE TABLE `storage`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '仓库名',
  `remark` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `capacity` int(0) NOT NULL DEFAULT 0 COMMENT '总容量',
  `used` int(0) NOT NULL DEFAULT 0 COMMENT '已使用量',
  `agv_slot` int(0) NULL DEFAULT NULL COMMENT 'AGV货架编号(1-48)，NULL表示未注册',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of storage
-- ----------------------------
INSERT INTO `storage` VALUES (1, '日用品仓库', '用于存放日用品', 1000, 200, 1);
INSERT INTO `storage` VALUES (2, '数码仓库', '用于存放数码产品', 500, 100, 2);
INSERT INTO `storage` VALUES (3, '食品仓库', '用于存放食品', 2000, 400, 3);
INSERT INTO `storage` VALUES (4, '冷冻仓库', '用于存放冷冻食品', 800, 200, 4);
INSERT INTO `storage` VALUES (5, '果蔬仓库', '用于存放水果和蔬菜', 1500, 500, 5);
INSERT INTO `storage` VALUES (6, '服装仓库', '用于存放服装', 1200, 0, 6);
INSERT INTO `storage` VALUES (7, '水产仓库', '用于存放水产品', 600, 200, 7);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '账号',
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '名字',
  `password` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '密码',
  `age` int(0) NULL DEFAULT NULL,
  `sex` int(0) NULL DEFAULT NULL COMMENT '性别',
  `phone` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '电话',
  `role_id` int(0) NULL DEFAULT NULL COMMENT '角色 0超级管理员，1管理员，2普通账号',
  `isValid` varchar(4) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT 'Y' COMMENT '是否有效，Y有效，其他无效',
  `workStatus` int(0) NOT NULL DEFAULT 0 COMMENT '工作状态 0=未上班 1=空闲 2=工作中 3=休息中',
  `todayRestMinutes` int(0) NOT NULL DEFAULT 0 COMMENT '今日已休息分钟数',
  `lastRestDate` date NULL DEFAULT NULL COMMENT '上次重置休息时间的日期',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'adminPromax', '超级管理员', '123456', 18, 1, '18855079621', 0, 'Y');
INSERT INTO `user` VALUES (2, 'admin', '普通管理员', '123456', 19, 0, '18855079621', 1, 'Y');
INSERT INTO `user` VALUES (3, 'user', '用户01', '123456', 20, 0, '18855079621', 2, 'Y');
INSERT INTO `user` VALUES (4, 'user03', '湫', '123456', 20, 1, '18855079621', 2, 'Y');
INSERT INTO `user` VALUES (5, 'user04', '朝菌', '123456', 18, 1, '18855079621', 2, 'Y');
INSERT INTO `user` VALUES (6, 'user05', '蟪蛄', '123456', 32, 0, '18855079621', 2, 'Y');
INSERT INTO `user` VALUES (7, 'user06', '鲲鹏', '123456', 26, 1, '18855079621', 2, 'Y');
INSERT INTO `user` VALUES (8, 'admin02', '管理员02', '123456', 24, 0, '18855079621', 1, 'Y');
INSERT INTO `user` VALUES (9, 'user02', '椿', '123456', 18, 0, '18855079621', 2, 'Y');

-- ----------------------------
-- Table structure for work_order
-- ----------------------------
DROP TABLE IF EXISTS `work_order`;
CREATE TABLE `work_order` (
  `id`          int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `goods_id`    int(0) NOT NULL COMMENT '货物id',
  `storage_id`  int(0) NOT NULL COMMENT '目标货架id',
  `count`       int(0) NOT NULL DEFAULT 1 COMMENT '数量',
  `type`        int(0) NOT NULL DEFAULT 0 COMMENT '类型 0=入库 1=出库',
  `deadline`    datetime(0) NULL DEFAULT NULL COMMENT '截止时间',
  `remark`      varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `admin_id`    int(0) NULL DEFAULT NULL COMMENT '派发管理员id',
  `worker_id`   int(0) NULL DEFAULT NULL COMMENT '指派工人id（assigned_to=0时有效）',
  `assigned_to` int(0) NOT NULL DEFAULT 0 COMMENT '指派对象 0=工人 1=AGV',
  `agv_id`      int(0) NULL DEFAULT NULL COMMENT '指派AGV编号（assigned_to=1时有效）',
  `status`      int(0) NOT NULL DEFAULT 0 COMMENT '状态 0=待处理 1=已完成',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `finish_time` datetime(0) NULL DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- 菜单：工单管理（仅管理员可见，roleId 0 和 1）
INSERT INTO `menu` VALUES (7, '007', '工单管理', '1', NULL, 'WorkOrder', '0,1', 'workorder/WorkOrderManage', 'el-icon-s-claim');

-- 如果 work_order 表已存在，执行此语句添加 type 列
-- ALTER TABLE `work_order` ADD COLUMN `type` int(0) NOT NULL DEFAULT 0 COMMENT '类型 0=入库 1=出库' AFTER `count`;

-- ----------------------------
-- 新增字段（对已有数据库执行以下 ALTER 语句）
-- ----------------------------

-- storage 表：新增总容量和已使用量字段
ALTER TABLE `storage` ADD COLUMN `capacity` int(0) NOT NULL DEFAULT 0 COMMENT '总容量' AFTER `remark`;
ALTER TABLE `storage` ADD COLUMN `used`     int(0) NOT NULL DEFAULT 0 COMMENT '已使用量' AFTER `capacity`;
-- 修复已有数据中 used 为 NULL 的情况
UPDATE `storage` SET `used` = 0 WHERE `used` IS NULL;
-- storage 表：新增 AGV 货架编号字段
ALTER TABLE `storage` ADD COLUMN `agv_slot` int(0) NULL DEFAULT NULL COMMENT 'AGV货架编号(1-48)，NULL表示未注册' AFTER `used`;
-- storage 表：新增 AGV 货架编号字段
ALTER TABLE `storage` ADD COLUMN `agv_slot` int(0) NULL DEFAULT NULL COMMENT 'AGV货架编号(1-48)，NULL表示未注册' AFTER `used`;
-- work_order 表：新增指派对象和 AGV 编号字段
ALTER TABLE `work_order` ADD COLUMN `assigned_to` int(0) NOT NULL DEFAULT 0 COMMENT '指派对象 0=工人 1=AGV' AFTER `worker_id`;
ALTER TABLE `work_order` ADD COLUMN `agv_id`      int(0) NULL DEFAULT NULL COMMENT '指派AGV编号' AFTER `assigned_to`;

-- user 表：新增工人状态机相关字段
ALTER TABLE `user` ADD COLUMN `workStatus`        int(0)  NOT NULL DEFAULT 0    COMMENT '工作状态 0=未上班 1=空闲 2=工作中 3=休息中' AFTER `isValid`;
ALTER TABLE `user` ADD COLUMN `todayRestMinutes`  int(0)  NOT NULL DEFAULT 0    COMMENT '今日已休息分钟数' AFTER `workStatus`;
ALTER TABLE `user` ADD COLUMN `lastRestDate`      date    NULL     DEFAULT NULL  COMMENT '上次重置休息时间的日期' AFTER `todayRestMinutes`;

-- ----------------------------
-- 集成 box-optimization：goods 和 storage 新增三维尺寸字段
-- ----------------------------

-- goods 表：新增长/高/宽（单位 cm）
ALTER TABLE `goods`
  ADD COLUMN `length` DOUBLE NOT NULL DEFAULT 1.0 COMMENT '长(cm)',
  ADD COLUMN `height` DOUBLE NOT NULL DEFAULT 1.0 COMMENT '高(cm)',
  ADD COLUMN `width`  DOUBLE NOT NULL DEFAULT 1.0 COMMENT '宽(cm)';

-- storage 表：新增长/高/宽（单位 cm）
ALTER TABLE `storage`
  ADD COLUMN `length` DOUBLE NOT NULL DEFAULT 500.0 COMMENT '长(cm)',
  ADD COLUMN `height` DOUBLE NOT NULL DEFAULT 300.0 COMMENT '高(cm)',
  ADD COLUMN `width`  DOUBLE NOT NULL DEFAULT 300.0 COMMENT '宽(cm)';

-- 为现有货物设置参考尺寸（cm）
UPDATE `goods` SET `length`=15.0, `height`=7.5,  `width`=7.0  WHERE id=1;   -- iPhone14
UPDATE `goods` SET `length`=15.0, `height`=8.0,  `width`=5.0  WHERE id=4;   -- 洁面乳
UPDATE `goods` SET `length`=20.0, `height`=12.0, `width`=15.0 WHERE id=5;   -- 葡萄
UPDATE `goods` SET `length`=25.0, `height`=15.0, `width`=15.0 WHERE id=6;   -- 西红柿
UPDATE `goods` SET `length`=30.0, `height`=10.0, `width`=20.0 WHERE id=7;   -- 皮皮虾
UPDATE `goods` SET `length`=20.0, `height`=10.0, `width`=15.0 WHERE id=8;   -- AD钙
UPDATE `goods` SET `length`=28.0, `height`=20.0, `width`=7.0  WHERE id=11;  -- iPad Air5

-- ----------------------------
-- Table structure for box_layout（装箱优化结果缓存）
-- ----------------------------
DROP TABLE IF EXISTS `box_layout`;
CREATE TABLE `box_layout` (
  `id`          int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `storage_id`  int(0) NOT NULL COMMENT '仓库ID',
  `layout_json` longtext COMMENT '装箱优化结果JSON',
  `updated_at`  datetime(0) NULL DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_storage_id` (`storage_id`)
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
