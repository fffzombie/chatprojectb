# ************************************************************
# Sequel Ace SQL dump
# 版本号： 20050
#
# https://sequel-ace.com/
# https://github.com/Sequel-Ace/Sequel-Ace
#
# 主机: 127.0.0.1 (MySQL 5.6.39)
# 数据库: openai
# 生成时间: 2023-10-08 01:32:51 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
SET NAMES utf8mb4;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE='NO_AUTO_VALUE_ON_ZERO', SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE database if NOT EXISTS `openai` default character set utf8mb4 collate utf8mb4_0900_ai_ci;
use `openai`;

# 转储表 openai_order
# ------------------------------------------------------------

DROP TABLE IF EXISTS `openai_order`;

CREATE TABLE `openai_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `openid` varchar(128) NOT NULL COMMENT '用户ID；微信分配的唯一ID编码',
  `product_id` int(4) NOT NULL COMMENT '商品ID',
  `product_name` varchar(32) NOT NULL COMMENT '商品名称',
  `product_quota` int(8) NOT NULL COMMENT '商品额度',
  `product_model_types` varchar(128) DEFAULT NULL COMMENT '可用模型；glm-3-turbo,glm-4,glm-4v,cogview-3,gpt-3.5-turbo,gpt-4,gpt-4o',
  `order_id` varchar(12) NOT NULL COMMENT '订单编号',
  `order_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `order_status` tinyint(1) NOT NULL COMMENT '订单状态；0-创建完成、1-等待发货、2-发货完成、3-系统关单',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单金额',
  `pay_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '支付方式；0-微信支付',
  `pay_url` varchar(128) DEFAULT NULL COMMENT '支付地址；创建支付后，获得的URL地址',
  `pay_amount` decimal(10,2) DEFAULT NULL COMMENT '支付金额；支付成功后，以回调信息更新金额',
  `transaction_id` varchar(32) DEFAULT NULL COMMENT '交易单号；支付成功后，回调信息的交易单号',
  `pay_status` tinyint(1) DEFAULT NULL COMMENT '支付状态；0-等待支付、1-支付完成、2-支付失败、3-放弃支付',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  KEY `idx_openid` (`openid`),
  KEY `idx_order_status_pay_status_order_time` (`order_time`,`order_status`,`pay_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

# 转储表 openai_product
# ------------------------------------------------------------

DROP TABLE IF EXISTS `openai_product`;

CREATE TABLE `openai_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `product_id` int(4) NOT NULL COMMENT '商品ID',
  `product_name` varchar(32) NOT NULL COMMENT '商品名称',
  `product_desc` varchar(128) NOT NULL COMMENT '商品描述',
  `product_model_types` varchar(256) DEFAULT NULL COMMENT '可用模型；glm-3-turbo,glm-4,glm-4v,cogview-3,gpt-3.5-turbo,gpt-4,gpt-4o',
  `quota` int(8) NOT NULL COMMENT '额度次数',
  `price` decimal(10,2) NOT NULL COMMENT '商品价格',
  `sort` int(4) NOT NULL COMMENT '商品排序',
  `is_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否有效；0无效、1有效',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id_enabled` (`product_id`,`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



# 转储表 user_account
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user_account`;

CREATE TABLE `user_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `openid` varchar(64) NOT NULL COMMENT '用户ID；这里用的是微信ID作为唯一ID，你也可以给用户创建唯一ID，之后绑定微信ID',
  `total_quota` int(11) NOT NULL DEFAULT '0' COMMENT '总量额度；分配的总使用次数',
  `surplus_quota` int(11) NOT NULL DEFAULT '0' COMMENT '剩余额度；剩余的可使用次数',
  `model_types` varchar(256) NOT NULL COMMENT '可用模型；chatglm_turbo,glm-3-turbo,glm-4,glm-4v,cogview-3',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '账户状态；0-可用、1-冻结',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_openid` (`openid`),
  KEY `idx_surplus_quota_status` (`surplus_quota`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
